package com.example.solarinsolationsih;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class Dashboard extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView textAzimuth, textAoE, textAoR;
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
        setContentView(R.layout.activity_dashboard);

        toolbar = findViewById(R.id.toolbar);
        textAzimuth = findViewById(R.id.azimuth);
        textAoE = findViewById(R.id.angleOfElevation);
        textAoR = findViewById(R.id.angleOfRotation);
        setSupportActionBar(toolbar);

        SensorManager manager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        manager.registerListener(sensorEventListener_for_gravity, manager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_UI);
        manager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.camera)
            startActivity(new Intent(Dashboard.this, CameraActivity.class));
        return true;
    }

    private float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + 0.03f * (input[i] - output[i]);
        }
        return output;
    }
}