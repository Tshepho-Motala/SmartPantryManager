package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AllRecipesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_recipes);
        setTitle("Recipe Collection");

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
    }
}
