package com.example.solarinsolationsih;

import android.Manifest;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Dashboard extends AppCompatActivity {

    private Toolbar toolbar;
    private BottomNavigationView bottomNav;
    private Fragment selectedFragment;


    BottomNavigationView.OnNavigationItemSelectedListener bottomNavListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            boolean flag = true;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    if (selectedFragment.toString().contains("HomeFragment"))
                        flag = false;
                    selectedFragment = new HomeFragment();
                    break;

                case R.id.nav_graph:
                    if (selectedFragment.toString().contains("GraphFragment"))
                        flag = false;
                    selectedFragment = new GraphFragment();
                    break;
            }
            Log.d("TAG", "Message1: " + selectedFragment.toString());
            if (flag) {
                Log.d("TAG", "Message2: " + selectedFragment.toString());
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }

            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 9);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 8);

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(bottomNavListener);
        selectedFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(Dashboard.this, CameraActivity.class);
        if (item.getItemId() == R.id.camera) {
            startActivity(intent);
        }

        return true;
    }


}