package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

public class AllRecipesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_recipes);
        setTitle("Recipe Collection");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Recipe> allRecipes = dbHelper.getAllRecipes();
        List<Ingredient> pantryItems = dbHelper.getAllPantryItems();

        RecyclerView recyclerView = findViewById(R.id.recyclerAllRecipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        RecipeAdapter adapter = new RecipeAdapter(allRecipes, pantryItems, recipe -> {
            Intent intent = new Intent(AllRecipesActivity.this, RecipeDetailActivity.class);
            intent.putExtra("RECIPE", recipe);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        setupBottomNav(R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBottomNav(R.id.nav_recipes);
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
                    return true;
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(this, SettingsActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
        }
    }
}
