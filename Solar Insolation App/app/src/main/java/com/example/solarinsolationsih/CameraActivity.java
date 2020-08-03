package com.example.solarinsolationsih;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import com.otaliastudios.cameraview.size.Size;
import com.otaliastudios.cameraview.size.SizeSelector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CameraActivity extends AppCompatActivity {

    private CameraView camera;
    private ImageView backButton;
    private ProgressBar progressBar;
    private byte[] imageArray;
    private ImageButton record;
    private Bitmap imageBitmap;
    private boolean mIsRecording = false;
    private File csvFile, frameFile;
    private StringBuilder csvData;
    private ArrayList<ArrayList<Float>> spatial_angles;
    private ArrayList<Bitmap> bitmapArrayList;
    private long prevTime;
    private float mAzimuth, mAngleOfElevation, mAngleOfRotation;
    private String postUrl, url;

    FrameProcessor frameProcessor = new FrameProcessor() {
        @Override
        public void process(@NonNull Frame frame) {
            long curTime = System.currentTimeMillis();
            if (mIsRecording && (curTime >= prevTime + 500)) {
                if (frame.getDataClass() == byte[].class) {
                    Log.d("TAG", "FrameProcessorMessage: Processing frames in byte array.");
                    File imageFile = new File(frameFile.getAbsolutePath(), System.currentTimeMillis() + ".jpg");
                    byte[] data = frame.getData();
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                        fileOutputStream.write(data);
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (frame.getDataClass() == Image.class) {
                    Log.d("TAG", "FrameProcessorMessage: Processing frames in image class.");
                    Log.d("TAG", "Image Rotation: " + frame.getRotationToUser() + ", " + frame.getRotationToView());
                    ArrayList<Float> values = new ArrayList<>();
                    values.add(mAzimuth);
                    values.add(mAngleOfElevation);
                    values.add(mAngleOfRotation);
                    spatial_angles.add(values);
                    long time = System.currentTimeMillis();
                    File imageFile = new File(frameFile.getAbsolutePath(), time + ".jpg");
                    csvData.append("\n").append(mAzimuth).append(", ").append(mAngleOfElevation).append(", ").append(mAngleOfRotation).append(", ").append(time);
                    Image data = frame.getData();
                    ByteBuffer buffer = data.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    Bitmap tempBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                    Matrix rotateRight = new Matrix();
                    rotateRight.preRotate(90);
                    Bitmap bitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), rotateRight, true);
                    bitmapArrayList.add(bitmap);
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                        fileOutputStream.close();
                        data.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                prevTime = curTime;
            }
        }
    };

    private SensorEventListener sensorEventListener_for_gravity = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            double angle = Math.atan2(x, y);
            mAngleOfRotation = Float.parseFloat(String.format(Locale.getDefault(), "%.2f", ((float) Math.toDegrees(angle))));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    private SensorEventListener sensorEventListener = new SensorEventListener() {

        private static final String TAG = "";
        float[] mGravity;
        float[] mGeomagnetic;

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            // Calculation of Azimuth, Angle of Elevation and angle of rotation

            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity = lowPass(sensorEvent.values, mGravity);
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic = lowPass(sensorEvent.values, mGeomagnetic);
            }


            if (mGravity != null && mGeomagnetic != null) {
                float[] R = new float[9];
                float[] I = new float[9];

                if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {

                    float[] orientationData = new float[3];
                    SensorManager.getOrientation(R, orientationData);

                    float root = (float) Math.sqrt((R[2] * R[2] + R[5] * R[5]));
                    float angle_of_elevation = (float) Math.acos(root / 1);
                    mAngleOfElevation = Float.parseFloat(String.format(Locale.getDefault(), "%.2f", ((float) Math.toDegrees(angle_of_elevation))));
                    if (R[8] >= 0) {
                        mAngleOfElevation *= -1;
                    }

                    // Calculation of Azimuth...

                    mAzimuth = (float) Math.atan2((R[1] - R[3]), (R[0] + R[4]));
                    mAzimuth = Float.parseFloat(String.format(Locale.getDefault(), "%.2f", ((float) Math.toDegrees(mAzimuth))));
                    if (mAzimuth < 0) {
                        mAzimuth = 360 + mAzimuth;
                    }
                }
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        camera = findViewById(R.id.cameraView);
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);
        record = findViewById(R.id.record);

        camera.setLifecycleOwner(this);
        camera.open();
        camera.setFrameProcessingFormat(ImageFormat.JPEG);
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        postUrl = url + "/predict";
        spatial_angles = new ArrayList<ArrayList<Float>>();
        bitmapArrayList = new ArrayList<>();

        SensorManager manager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        manager.registerListener(sensorEventListener_for_gravity, manager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_UI);
        manager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsRecording) {
                    record.setImageResource(R.mipmap.ic_video_green);
                    mIsRecording = false;
                    camera.removeFrameProcessor(frameProcessor);
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(csvFile);
                        fileOutputStream.write(csvData.toString().getBytes());
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    connectServer();

                } else {
                    record.setImageResource(R.mipmap.ic_video_red);
                    createFile();
                    camera.addFrameProcessor(frameProcessor);
                    mIsRecording = true;
                    prevTime = System.currentTimeMillis();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void connectServer() {

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (int i = 0; i < bitmapArrayList.size(); i++) {

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap bitmap = bitmapArrayList.get(i);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            multipartBodyBuilder.addFormDataPart("image" + i, "Android_Flask_" + i + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));
        }

        RequestBody postBodyImage = multipartBodyBuilder.build();
        postRequest(postUrl, postBodyImage);

    }

    private void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .connectTimeout(200, TimeUnit.SECONDS)
                .writeTimeout(200, TimeUnit.SECONDS)
                .readTimeout(400, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("TAG", "Message: Failed to Connect to Server. Please Try Again. " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView response = findViewById(R.id.response);
                        response.setText("Failed to connect to server. Please try again.");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Log.d("TAG", "Server's Response\n" + response.body().string());
                camera.close();
                camera.destroy();
                Intent intent = new Intent(CameraActivity.this, MaskedImages.class);
                intent.putExtra("spacialAngles", spatial_angles);
                intent.putExtra("numberOfImages", bitmapArrayList.size());
                startActivity(intent);
            }
        });
    }

    private void createFile() {
        File dir = this.getExternalFilesDir(null);
        assert dir != null;
        File file = new File(dir.getAbsolutePath(), "/" + System.currentTimeMillis() + "/");
        frameFile = new File(file.getAbsolutePath(), "/Frames/");
        frameFile.mkdirs();
        csvFile = new File(file.getAbsolutePath(), "Data.csv");
        csvData = new StringBuilder();
        csvData.append("Azimuth, Angle of Elevation, Angle of rotation, Time");
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.close();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        camera.setLifecycleOwner(this);
        camera.open();
    }

    private float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + 0.03f * (input[i] - output[i]);
        }
        return output;
    }

    private void serverResponse() {
        progressBar.setVisibility(View.GONE);
        TextView response = findViewById(R.id.response);
        response.setText("Server error.");
    }

}