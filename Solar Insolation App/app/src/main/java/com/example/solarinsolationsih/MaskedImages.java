package com.example.solarinsolationsih;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MaskedImages extends AppCompatActivity {
    ArrayList<Bitmap> imageArray;
    ArrayList<ArrayList<Float>> spatialValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masked_images);
        ImageView imageView = findViewById(R.id.imageView);
        imageArray = new ArrayList<>();
        spatialValues = new ArrayList<ArrayList<Float>>();
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        int numberOfImages = intent.getIntExtra("numberOfImages", 0);
        String spatialAngles = intent.getStringExtra("values");
        File dir = this.getExternalFilesDir(null);
        final FileOutputStream[] fileOutputStream = {null};

        for (int i = 0; i < numberOfImages; i++) {
            String finalUrl = url + "/files/mask_Android_Flask_" + i + ".jpg";
            final File file = new File(dir.getAbsolutePath(), "Image_" + i + ".jpg");
            Picasso.get()
                    .load(finalUrl)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            try {
                                fileOutputStream[0] = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream[0]);
                                fileOutputStream[0].close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            Toast.makeText(MaskedImages.this, "Failed to retrieve images.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
        }
        String[] textValues = spatialAngles.split("\n");

        for (int i = 0; i < numberOfImages; i++) {
            File file = new File(dir.getAbsolutePath(), "Image_" + i + ".jpg");
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            imageArray.add(bitmap);

            if (i != 0) {
                String[] newText = textValues[i].split(",");
                ArrayList<Float> temporary = new ArrayList<>();
                for (int j = 0; j < newText.length; j++) {
                    temporary.add(Float.valueOf(newText[i]));
                }
                spatialValues.add(temporary);
            }
        }
        Toast.makeText(this, "Image array: " + imageArray.size(), Toast.LENGTH_SHORT).show();

        ImageProcessing imageProcessing = new ImageProcessing();
        imageProcessing.setImageBitmaps(imageArray);
        imageProcessing.setSpatial_angles(spatialValues);
        try {
            imageProcessing.REFINE_SPATIAL_ANGLES();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}