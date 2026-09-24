package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textZeroMatch;
    private DatabaseHelper dbHelper;
    private RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setTitle("Suggested Recipes");

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerSuggestedRecipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        textZeroMatch = findViewById(R.id.textZeroMatch);

        loadSuggestedRecipes();
    }

    private void loadSuggestedRecipes() {
        List<Recipe> allRecipes = dbHelper.getAllRecipes();
        List<Ingredient> pantryItems = dbHelper.getAllPantryItems();

        List<Recipe> suggested = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            boolean canMake = true;
            for (Recipe.RecipeIngredient req : recipe.getRequiredIngredients()) {
                boolean found = false;
                for (Ingredient p : pantryItems) {
                    if (isIngredientMatch(req.getName(), p.getName()) && p.getQuantity() >= req.getRequiredQuantity()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    canMake = false;
                    break;
                }
            }
            if (canMake) {
                suggested.add(recipe);
            }
        }

        if (suggested.isEmpty()) {
            textZeroMatch.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textZeroMatch.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new RecipeAdapter(suggested, pantryItems, recipe -> {
                Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
                intent.putExtra("RECIPE", recipe);
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        }
    }

    private boolean isIngredientMatch(String reqName, String pantryName) {
        String req = reqName.trim().toLowerCase();
        String pantry = pantryName.trim().toLowerCase();
        if (req.equals(pantry)) return true;
        if (pantry.endsWith("es") && pantry.substring(0, pantry.length() - 2).equals(req)) return true;
        if (req.endsWith("es") && req.substring(0, req.length() - 2).equals(pantry)) return true;
        if (pantry.endsWith("s") && pantry.substring(0, pantry.length() - 1).equals(req)) return true;
        if (req.endsWith("s") && req.substring(0, req.length() - 1).equals(pantry)) return true;
        return false;
    }
}
