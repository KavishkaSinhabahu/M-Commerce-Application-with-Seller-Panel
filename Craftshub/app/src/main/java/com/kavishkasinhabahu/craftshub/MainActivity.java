package com.kavishkasinhabahu.craftshub;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ProximitySensorManager proximitySensorManager;
    private FlightModeReceiver flightModeReceiver = new FlightModeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        startService(new Intent(this, ShakeWakeService.class));
        proximitySensorManager = new ProximitySensorManager(this);
        proximitySensorManager.register();
        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(flightModeReceiver, filter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnApplyWindowInsetsListener(null);
        bottomNavigationView.setPadding(0, 0, 0, 0);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else if (item.getItemId() == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (item.getItemId() == R.id.nav_profile) {

                handleProfileNavigation();
                return true;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack(null)
                        .commit();
            }

            return true;
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(flightModeReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            unregisterReceiver(flightModeReceiver);
        } catch (IllegalArgumentException e) {
            Log.d("Craftshub-Log", "Receiver not registered, skipping unregister: " + e.getMessage());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (proximitySensorManager != null) {
            proximitySensorManager.unregister();
        }

        try {
            unregisterReceiver(flightModeReceiver);
        } catch (IllegalArgumentException e) {
            Log.d("Craftshub-Log", "Receiver not registered, skipping unregister: " + e.getMessage());
        }
    }

    private void handleProfileNavigation() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String userType = sharedPreferences.getString("userType", "user"); // Default to "user" if not found

        if (!isLoggedIn) {
            Toast.makeText(this, "Login first!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, SigninActivity.class);
            startActivity(intent);
        } else {
            if ("seller".equals(userType)) {
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .addToBackStack(null)
                        .commit();
            }
        }
    }
}