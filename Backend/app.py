import flask
import torch
import torchvision
import io
import numpy as np
from PIL import Image
import cv2
import matplotlib.pyplot as plt
import os
import net
import werkzeug
from tqdm import tqdm
from time import time


UPLOAD_FOLDER = os.path.join(os.getcwd(), "uploads")

app = flask.Flask(__name__)

if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

app.config["UPLOAD_FOLDER"] = UPLOAD_FOLDER

@app.route("/files")
def list_files():
    """ List of all files in UPLOAD_FOLDER """

    files = []
    for filename in os.listdir(UPLOAD_FOLDER):
        path = os.path.join(UPLOAD_FOLDER, filename)
        if os.path.isfile(path):
            files.append(filename)
    return flask.jsonify(files)

@app.route("/files/<path:path>")
def get_file(path):
    return flask.send_file( os.path.join(UPLOAD_FOLDER, path), as_attachment=True)

model = torch.load("SS_CustomData1200_mobilenet.pt", map_location = torch.device("cpu"))
model.eval()

def prepare_image(image_bytes):
    """prepare image for sky segmentation model"""

    mean = [0.485, 0.456, 0.406]
    std = [0.229, 0.224, 0.225]

    image = Image.open(io.BytesIO(image_bytes))
    image = np.asarray(image)
    org_shape = image.shape[:2]
    resized = cv2.resize(image, (512, 512), interpolation= cv2.INTER_NEAREST)
    transposed = np.transpose(resized, (2, 0, 1))
    image_tensor = torch.from_numpy(transposed).float()
    image = normalize(image_tensor, mean, std)
    image = image[None,...]
    return image, org_shape

def normalize(tensor, mean, std):
    """normalize image for sky segmentation model"""

    tensor_copy = tensor.clone()
    for t, m, s in zip(tensor_copy, mean, std):
        t.sub_(m).div_(s)
    return tensor_copy

def get_prediction(input_tensor):
    """feed forward input tensor into sky segmentation model and get mask"""

    output = model(input_tensor)            
    probabilities = torch.sigmoid(output)
    one = 255.0*torch.ones_like(probabilities)
    zero = torch.zeros_like(probabilities)
    prediction = torch.where(probabilities < 0.5 , zero, one)[0][0]
    return prediction.numpy()

def get_largest(num, comp):
    """
    Get largest connected region

    Args:
        num: Number of connected regions
        comp: regions labeled

    Returns: largest region label
    """
    size = {i:0 for i in range(num) if i != 0}
    for i in range(1,num):
        for pix in comp.reshape(-1):
            if pix == i:
                size[i] += 1
    comp_largest = [label for label in size if size[label] == max(size.values())][0]
    return comp_largest, size

def clean_mask(mask, iterations):
    for i in range(iterations): 
        thresh = np.uint8(cv2.threshold(mask, 127, 255, cv2.THRESH_BINARY)[1])
        inv_thresh = np.uint8(np.where(thresh == 255, 0, 255))

        num, comp = cv2.connectedComponents(thresh)
        inv_num, inv_comp = cv2.connectedComponents(inv_thresh)

        comp_largest, size = get_largest(num, comp)
        inv_comp_largest, inv_size = get_largest(inv_num, inv_comp)

        clean_mask = thresh.copy()

        for y in range(clean_mask.shape[0]):
            for x in range(clean_mask.shape[1]):
                if ((comp[y,x] != comp_largest) and (comp[y,x] != 0)):
                    clean_mask[y,x] = 0
                elif ((inv_comp[y,x] != inv_comp_largest) and (inv_comp[y,x] != 0)):
                    clean_mask[y,x] = 255
        mask = clean_mask
    return mask


@app.route("/", methods = ["GET"])
def root():
    """home page"""

    return flask.jsonify({"msg": "Try POSTing to the /predict endpoint with an RGB image attachment"})

@app.route("/predict", methods = ["POST"])
def predict():
    if flask.request.method == "POST":
        total_start = time()
        # DELETE ALL FILES in UPLOAD_FOLDER
        files = os.listdir(app.config["UPLOAD_FOLDER"])
        if len(files) == 0:
            pass
        else:
            for f in files:
                os.remove( os.path.join(app.config["UPLOAD_FOLDER"], f) )
        # UPLOAD
        image_ids = list(flask.request.files)
        image_num = 0
        filepaths = list()
        print("uploading...")
        upload_start = time()
        for image_id in tqdm(image_ids):
            imagefile = flask.request.files[image_id]
            filename = werkzeug.utils.secure_filename(imagefile.filename)
            filepath = os.path.join(app.config["UPLOAD_FOLDER"], filename)
            imagefile.save(filepath)
            filepaths.append(filepath)
            image_num += 1
        upload_end = time()
        upload_time = upload_end - upload_start
        print("Done! , {} files uploaded in {} min. {:.3f} sec.".format(image_num, upload_time//60,upload_time%60))

        # SEGMENT SKY

        print("segmenting...")
        seg_start = time()
        seg_cnt = 0
        for filepath in tqdm(filepaths):
            fp = open(filepath, "rb")
            img_bytes = fp.read()
            filename = filepath.split("\\")[-1]
            input_tensor, in_shape = prepare_image(img_bytes)
            prediction = get_prediction(input_tensor)
            final_output = cv2.resize(prediction, (in_shape[1],in_shape[0]), interpolation= cv2.INTER_NEAREST)
            cleaned_mask = clean_mask(final_output, 2)
            #print(final_output.shape)
            maskname = "mask_" + filename
            #print(np.unique(cleaned_mask))
            cv2.imwrite(os.path.join(app.config["UPLOAD_FOLDER"], maskname),cleaned_mask)
            seg_cnt += 1
        seg_end = time()
        seg_time = seg_end - seg_start
        
        total_end = time()
        total_time = total_end - total_start
        print("Done! , {} images segmented in {} min. {:.3f} sec.".format(seg_cnt, seg_time//60, seg_time%60))
        print("OPERATION SUCCESSFUL ;)")
        return ":)"

@app.route("/dehaze/<filename>",methods=["POST"])
def dehaze(filename):
    if "/" in filename:
        flask.abort(400, "no subdirectories directories allowed")

    image_path = os.path.join(UPLOAD_FOLDER, filename)
    dehazed_name = "dehazed_" + filename
    dehazed_path = os.path.join(UPLOAD_FOLDER, dehazed_name)
    data_hazy = Image.open(image_path)
    data_hazy = (np.asarray(data_hazy)/255.0)
    data_hazy = torch.from_numpy(data_hazy).float()
    data_hazy = data_hazy.permute(2, 0, 1)
    data_hazy = data_hazy.unsqueeze(0)
    dehaze_net = net.dehaze_net()
    dehaze_net.load_state_dict(torch.load("dehazer.pth", map_location= torch.device("cpu")))
    clean_image = dehaze_net(data_hazy)
    torchvision.utils.save_image(clean_image, dehazed_path)
    #print(clean_image.shape)
    return flask.jsonify({"dehazed": dehazed_path})

"""
from climlab.solar.insolation import daily_insolation

@app.route("/dailyinsolation<params>")
def dailyinsolation(params):
    insolation = daily_insolation(params["lat"] , params["day"])
    return flask.jsonify({"insolation": float(insolation)})
"""

if __name__ == "__main__":
    app.run()

