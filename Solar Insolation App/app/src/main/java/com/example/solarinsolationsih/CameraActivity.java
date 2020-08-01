package com.example.solarinsolationsih;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

    private CameraView camera;
    private ImageButton capture;
    private File pictureFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        camera = findViewById(R.id.cameraView);
        capture = findViewById(R.id.capture);
        camera.setLifecycleOwner(this);
        camera.open();

        camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                byte[] image = result.getData();
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(pictureFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFile();
                camera.takePicture();
            }
        });
    }

    private void createFile() {
        File dir = this.getExternalFilesDir(null);
        pictureFile = new File(dir.getAbsolutePath(), "image.jpg");
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
}