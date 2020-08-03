package com.example.solarinsolationsih;

import android.app.Activity;
import android.content.ClipData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private TextView textAzimuth, textAoE, textAoR;
    static float latitude;
    static float longitude;
    private EditText mLatitude, mLongitude, url;
    private boolean flag1 = false;
    private ArrayList<Bitmap> imageArray;
    private Button getLocation, calculate, selectFrames, selectData, submit, go;
    private float mAzimuth, mAngleOfElevation, mAngleOfRotation;
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private SensorEventListener sensorEventListener_for_gravity = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            double angle = Math.atan2(x, y);
            mAngleOfRotation = Float.parseFloat(String.format(Locale.getDefault(), "%.1f", ((float) Math.toDegrees(angle))));
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
                    mAngleOfElevation = Float.parseFloat(String.format(Locale.getDefault(), "%.1f", ((float) Math.toDegrees(angle_of_elevation))));
                    if (R[8] >= 0) {
                        mAngleOfElevation *= -1;
                        textAoE.setText(String.valueOf(mAngleOfElevation)); //NEGATIVE ANGLE OF ELEVATION
                    } else {
                        textAoE.setText(String.valueOf(mAngleOfElevation));
                    }

                    // Calculation of Azimuth...

                    mAzimuth = (float) Math.atan2((R[1] - R[3]), (R[0] + R[4]));
                    mAzimuth = Float.parseFloat(String.format(Locale.getDefault(), "%.1f", ((float) Math.toDegrees(mAzimuth))));
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        textAzimuth = view.findViewById(R.id.azimuth);
        textAoE = view.findViewById(R.id.angleOfElevation);
        textAoR = view.findViewById(R.id.angleOfRotation);
        getLocation = view.findViewById(R.id.getLocation);
        calculate = view.findViewById(R.id.calculate);
        selectFrames = view.findViewById(R.id.selectFrames);
        selectData = view.findViewById(R.id.selectData);
        submit = view.findViewById(R.id.submit);
        go = view.findViewById(R.id.go);
        mLatitude = view.findViewById(R.id.latitude);
        mLongitude = view.findViewById(R.id.longitude);
        url = view.findViewById(R.id.url);

        imageArray = new ArrayList<>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        SensorManager manager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        manager.registerListener(sensorEventListener_for_gravity, manager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_UI);
        manager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);


        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocation();
            }
        });

        selectFrames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select frames"), 99);
            }
        });

        selectData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/*");
                startActivityForResult(Intent.createChooser(intent, "Select data"), 90);
            }
        });

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(url.getText().toString())) {
                    url.setError("Enter url first");
                } else {
                    Intent cameraIntent = new Intent(getActivity(), CameraActivity.class);
                    cameraIntent.putExtra("url", url.getText().toString());
                    startActivity(cameraIntent);
                }
            }
        });

        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(TextUtils.isEmpty(mLatitude.getText().toString()) || TextUtils.isEmpty(mLongitude.getText().toString()))) {
                    latitude = Float.parseFloat(mLatitude.getText().toString());
                    longitude = Float.parseFloat(mLongitude.getText().toString());
                    getData();
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setLocation();
        getData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        try {
            if (requestCode == 99 && resultCode == Activity.RESULT_OK && data != null) {
                if (data.getData() != null) {
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        imageArray.add(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {
                        Uri uri = mClipData.getItemAt(i).getUri();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                            imageArray.add(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (requestCode == 90 && resultCode == Activity.RESULT_OK && data != null) {
                File file = new File(data.getData().getPath());
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Log.d("TAG", "CSV: " + line);
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Something went wrong." + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setLocation() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Enable location").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            } else {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            latitude = Float.parseFloat(String.format(Locale.getDefault(), "%.2f", ((float) location.getLatitude())));
                            longitude = Float.parseFloat(String.format(Locale.getDefault(), "%.2f", ((float) location.getLongitude())));
                            mLatitude.setText(String.valueOf(latitude));
                            mLongitude.setText(String.valueOf(longitude));
                        } else {
                            Toast.makeText(getContext(), "Unable to get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    private void getData() {
        String postUrl = "http://192.168.0.108:5000/calculate";
        String postBodyText = latitude + "_" + 216;
        MediaType mediaType = MediaType.parse("text/plain; chartset=utf-8");
        RequestBody postBody = RequestBody.create(mediaType, postBodyText);
        postRequest(postUrl, postBody);
    }

    private void postRequest(String postUrl, RequestBody postBody) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                final String[] data = responseBody.split("_");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView dailyInsolation, yearlyInsolation;
                        dailyInsolation = getView().findViewById(R.id.dailyInsolation);
                        yearlyInsolation = getView().findViewById(R.id.yearlyInsolation);
                        float dailyInsolationValue = Float.parseFloat(data[0]) / 100;
                        float yearlyInsolationValue = Float.parseFloat(data[data.length -1])/ 100;
                        dailyInsolation.setText(String.valueOf(dailyInsolationValue));
                        yearlyInsolation.setText(String.valueOf(yearlyInsolationValue));

                        TextView jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec;
                        jan = getView().findViewById(R.id.janInso);
                        feb = getView().findViewById(R.id.febInso);
                        mar = getView().findViewById(R.id.marInso);
                        apr = getView().findViewById(R.id.aprInso);
                        may = getView().findViewById(R.id.mayInso);
                        jun = getView().findViewById(R.id.junInso);
                        jul = getView().findViewById(R.id.julInso);
                        aug = getView().findViewById(R.id.augInso);
                        sep = getView().findViewById(R.id.sepInso);
                        oct = getView().findViewById(R.id.octInso);
                        nov = getView().findViewById(R.id.novInso);
                        dec = getView().findViewById(R.id.decInso);

                        jan.setText(String.format(Locale.getDefault(), "%.2f", (Float.parseFloat(data[1]) / 100)));
                        feb.setText(String.format(Locale.getDefault(), "%.2f", (Float.parseFloat(data[2]) / 100)));
                        mar.setText(String.format(Locale.getDefault(), "%.2f", (Float.parseFloat(data[3]) / 100)));
                        apr.setText(String.format(Locale.getDefault(), "%.2f", (Float.parseFloat(data[4]) / 100)));
                        may.setText(String.format(Locale.getDefault(), "%.2f", (Float.parseFloat(data[5]) / 100)));
                        jun.setText(String.format(Locale.getDefault(), "%.2f", (Float.parseFloat(data[6]) / 100)));
                        jul.setText(String.format(Locale.getDefault(), "%.2f", (Float.parseFloat(data[7]) / 100)));
                        aug.setText(String.format(Locale.getDefault(), "%.2f", (Float.parseFloat(data[8]) / 100)));
                        sep.setText(String.format(Locale.getDefault(), "%.2f", (Float.parseFloat(data[9]) / 100)));
                        oct.setText(String.format(Locale.getDefault(), "%.2f", (Float.parseFloat(data[10]) / 100)));
                        nov.setText(String.format(Locale.getDefault(), "%.2f", (Float.parseFloat(data[11]) / 100)));
                        dec.setText(String.format(Locale.getDefault(), "%.2f", (Float.parseFloat(data[12]) / 100)));
                    }
                });
            }
        });
    }

    private float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + 0.03f * (input[i] - output[i]);
        }
        return output;
    }
}