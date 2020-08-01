package com.example.solarinsolationsih;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;

import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
    private ImageButton capture;
    private ImageView backButton;
    private TextView textAzimuth, textAoE, textAoR;
    private byte[] imageArray;
    private Bitmap imageBitmap;
    private File pictureFile;
    private String postUrl;
    private float mAzimuth, mAngleOfElevation, mAngleOfRotation;

    private SensorEventListener sensorEventListener_for_gravity = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            double angle = Math.atan2(x, y);
            mAngleOfRotation = Float.parseFloat(String.format(Locale.getDefault(), "%.2f", ((float) Math.toDegrees(angle))));
            textAoR.setText(String.valueOf(mAngleOfRotation));
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
                        textAoE.setText(String.valueOf(mAngleOfElevation)); //NEGATIVE ANGLE OF ELEVATION
                    } else {
                        textAoE.setText(String.valueOf(mAngleOfElevation));
                    }

                    // Calculation of Azimuth...

                    mAzimuth = (float) Math.atan2((R[1] - R[3]), (R[0] + R[4]));
                    mAzimuth = Float.parseFloat(String.format(Locale.getDefault(), "%.2f", ((float) Math.toDegrees(mAzimuth))));
                    if (mAzimuth < 0) {
                        mAzimuth = 360 + mAzimuth;
                    }
                    textAzimuth.setText(String.valueOf(mAzimuth));
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
        capture = findViewById(R.id.capture);
        backButton = findViewById(R.id.backButton);
        textAzimuth = findViewById(R.id.azimuth);
        textAoE = findViewById(R.id.angleOfElevation);
        textAoR = findViewById(R.id.angleOfRotation);

        camera.setLifecycleOwner(this);
        camera.open();

        postUrl = "http://192.168.0.108:5000/predict";

        SensorManager manager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        manager.registerListener(sensorEventListener_for_gravity, manager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_UI);
        manager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                imageArray = result.getData();
                Bitmap tempBitmap = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length, null);
                Matrix rotateRight = new Matrix();
                rotateRight.preRotate(90);
                imageBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), rotateRight, true);
                connectServer();
            }
        });

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFile();
                camera.takePicture();
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

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();

        postRequest(postUrl, postBodyImage);
    }

    private void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

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
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Log.d("TAG", "Server's Response\n" + response.body().string());
                camera.close();
            }
        });
    }

    private void createFile() {
        File dir = this.getExternalFilesDir(null);
        pictureFile = new File(dir.getAbsolutePath(), "image_" + System.currentTimeMillis() + ".jpg");
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


}