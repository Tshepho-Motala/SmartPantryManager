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
    private PantryAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Ingredient> ingredientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);
        setTitle("Smart Pantry Inventory");

        dbHelper = new DatabaseHelper(this);
        recyclerViewPantry = findViewById(R.id.recyclerViewPantry);
        recyclerViewPantry.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(PantryListActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_pantry);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_pantry) {
                    return true;
                } else if (id == R.id.nav_suggested) {
                    startActivity(new Intent(PantryListActivity.this, SuggestedRecipesActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_recipes) {
                    startActivity(new Intent(PantryListActivity.this, AllRecipesActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(PantryListActivity.this, SettingsActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
        }

        loadPantryItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_pantry);
        }
    }

    private void loadPantryItems() {
        ingredientList = dbHelper.getAllPantryItems();
        adapter = new PantryAdapter(ingredientList, new PantryAdapter.OnItemClickListener() {
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
                dbHelper.deletePantryItem(selectedIngredient.getId());
                Toast.makeText(PantryListActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                loadPantryItems();
            }
        });
        recyclerViewPantry.setAdapter(adapter);
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
