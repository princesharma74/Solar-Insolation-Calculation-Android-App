package com.example.solarinsolationsih;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class MaskedImages extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masked_images);
        ImageView imageView = findViewById(R.id.imageView);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String finalUrl = url + "/files/mask_androidFlask.jpg";

        Picasso.get()
                .load(finalUrl)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(imageView);
    }
}