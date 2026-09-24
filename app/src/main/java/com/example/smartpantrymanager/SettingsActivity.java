package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setupBottomNav(R.id.nav_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBottomNav(R.id.nav_settings);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBottomNav(int selectedItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(selectedItemId);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == selectedItemId) {
                    return true;
                }
                if (id == R.id.nav_pantry) {
                    startActivity(new Intent(this, PantryListActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_suggested) {
                    startActivity(new Intent(this, SuggestedRecipesActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_recipes) {
                    startActivity(new Intent(this, AllRecipesActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_settings) {
                    return true;
                }
                return false;
            });
        }
    }
}
