# -*- coding: utf-8 -*-
"""
Created on Tue Mar 24 21:29:03 2020

@author: vivek
"""

import cv2
import os
import numpy as np
import functional as F
import yaml
import utils
import torch
from pathlib import Path

def get_mask_path(path , masks_dir):
    """
    Get Mask path given image path

    Args:
        path: {Path} -- Image Path
        masks_dir: {str} -- Masks Directory

    Returns:
        {Path} -- Mask Path
    """
    mask_path = Path(masks_dir).resolve().joinpath(path.parent.stem)
    return mask_path.with_suffix(".png")

def get_filtered_images(df, images_dir, masks_dir, filter=None):
    """
    Apply filter to df to get desired Images' paths

    Args:
        df -- {DataFrame} -- apply condition on
        images_dir -- {str} -- Path to images
        masks_dir -- {str} -- Path to masks
    Returns:
        {list of tuples of str} ,where  tuple = (image_path , mask_path)
    """
    if filter is None:
        image_paths = sorted(Path(images_dir).glob("**/*.jpg"))
    else:
        image_paths = []
        for idx in df[filter].index:
            image_paths.append(Path(images_dir).resolve().
                               joinpath(str(df["CamId"].iloc[idx])).
                               joinpath(df["Filename"].iloc[idx]))
    mask_paths = [str(get_mask_path(image_path, masks_dir)) for image_path in image_paths]
    image_paths = list(map(str, image_paths))

    return list(zip(image_paths, mask_paths))


class Rescale(object):
    """ Resize Image and Mask.

        if {args is int} preserve aspect ratio  else not
    Args:
        newshape {int, tuple} -- Output Image and Mask height and width
    """
    def __init__(self,newshape):
        assert isinstance(newshape, (int, tuple))
        self.newshape = newshape

    def __call__(self, sample):
        image = sample["image"]
        mask = sample["mask"]

        h,w = image.shape[:2]

        if isinstance(self.newshape, int):
            if h > w:
                new_h, new_w = self.newshape * h / w, self.newshape
            else:
                new_h, new_w = self.newshape, self.newshape * w / h
        else:
            new_h, new_w = self.newshape

        newshape = (int(new_w) , int(new_h))

        image = cv2.resize(image ,  newshape,interpolation=cv2.INTER_NEAREST)
        mask = cv2.resize(mask , newshape , interpolation=cv2.INTER_NEAREST)

        return {"image": image,
                "mask": mask}

class RandomCrop(object):
    """Crop randomly the image and mask

    Args:
        output_size {int, tuple} --  Desired output size. If int, square crop
            is made.
    """

    def __init__(self, output_size,width_only=False):
        assert isinstance(output_size, (int, tuple))
        self.width_only = width_only
        if isinstance(output_size, int):
            self.output_size = (output_size, output_size)
        else:
            assert len(output_size) == 2
            self.output_size = output_size
        
    def __call__(self, sample):
        image, mask = sample['image'], sample['mask']

        h, w = image.shape[:2]
        new_h, new_w = self.output_size

        left = np.random.randint(0, w - new_w)
        
        if self.width_only:
            #print("img , msk: {}".format(image.shape, mask.shape))
            image = image[:, left: left + new_w, :]
            mask = mask[:, left: left + new_w]
        else:
            top = np.random.randint(0, h - new_h)

            image = image[top: top + new_h,
                            left: left + new_w,:]
            mask = mask[top: top + new_h,
                            left: left + new_w]

        return {'image': image, 'mask': mask}

class Normalize_Image_and_Correct_Mask(object):

    def __init__(self,mean,std):
        self.mean = mean
        self.std = std
        #self.inplace = inplace

    def __call__(self,sample):
        sample = utils.correct_shape(sample)
        mask = utils.correct_binary(sample["mask"])
        
        image = torch.from_numpy(sample["image"].astype(np.float32))
        mask = torch.from_numpy(mask)[None,...]

        image = F.normalize(image, self.mean, self.std)[None,...]


        return {"image": image, "mask": mask}

class Compose(object):

    def __init__(self, transforms):
        assert isinstance(transforms , list)
        self.transforms = transforms

    def __call__(self, sample):
        for transform in self.transforms:
            sample = transform(sample)    
        return sample