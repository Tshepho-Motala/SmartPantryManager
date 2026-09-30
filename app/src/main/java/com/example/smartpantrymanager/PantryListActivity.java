package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class PantryListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPantry;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);
        setTitle("Smart Pantry Inventory");

        dbHelper = DatabaseHelper.getInstance(this);
        recyclerViewPantry = findViewById(R.id.recyclerViewPantry);
        recyclerViewPantry.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(PantryListActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });

        setupBottomNav(R.id.nav_pantry);
        loadPantryItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
        setupBottomNav(R.id.nav_pantry);
    }

    private void setupBottomNav(int selectedItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(null);
            bottomNav.setSelectedItemId(selectedItemId);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == selectedItemId) {
                    return true;
                }
                if (id == R.id.nav_pantry) {
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
                    startActivity(new Intent(this, SettingsActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
        }
    }

    private void loadPantryItems() {
        new Thread(() -> {
            List<Ingredient> ingredientList = dbHelper.getAllPantryItems();
            runOnUiThread(() -> {
                PantryAdapter adapter = new PantryAdapter(ingredientList, new PantryAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Ingredient ingredient) {
                        editItem(ingredient);
                    }

                    @Override
                    public void onEditClick(Ingredient selectedIngredient) {
                        editItem(selectedIngredient);
                    }

                    @Override
                    public void onDeleteClick(Ingredient selectedIngredient) {
                        new Thread(() -> {
                            dbHelper.deletePantryItem(selectedIngredient.getId());
                            runOnUiThread(() -> {
                                Toast.makeText(PantryListActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                                loadPantryItems();
                            });
                        }).start();
                    }
                });
                recyclerViewPantry.setAdapter(adapter);
            });
        }).start();
    }

    private void editItem(Ingredient ingredient) {
        Intent intent = new Intent(PantryListActivity.this, AddEditIngredientActivity.class);
        intent.putExtra("INGREDIENT_ID", ingredient.getId());
        intent.putExtra("INGREDIENT_NAME", ingredient.getName());
        intent.putExtra("INGREDIENT_QUANTITY", ingredient.getQuantity());
        intent.putExtra("INGREDIENT_UNIT", ingredient.getUnit());
        startActivity(intent);
    }
}
