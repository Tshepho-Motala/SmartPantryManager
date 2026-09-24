package com.example.smartpantrymanager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Recipe recipe = (Recipe) getIntent().getSerializableExtra("RECIPE");
        if (recipe == null) {
            finish();
            return;
        }

        setTitle(recipe.getTitle());

        TextView textTitle = findViewById(R.id.textDetailTitle);
        TextView textStatus = findViewById(R.id.textDetailStatus);
        TextView textIngredients = findViewById(R.id.textDetailIngredients);
        TextView textInstructions = findViewById(R.id.textDetailInstructions);

        textTitle.setText(recipe.getTitle());

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Ingredient> pantryItems = dbHelper.getAllPantryItems();

        int missingCount = 0;
        StringBuilder ingBuilder = new StringBuilder();
        for (Recipe.RecipeIngredient req : recipe.getRequiredIngredients()) {
            boolean hasEnough = false;
            for (Ingredient p : pantryItems) {
                if (isIngredientMatch(req.getName(), p.getName()) && p.getQuantity() >= req.getRequiredQuantity()) {
                    hasEnough = true;
                    break;
                }
            }
            if (hasEnough) {
                ingBuilder.append("• ").append(req.getName()).append(": ").append(req.getRequiredQuantity()).append(" ").append(req.getUnit()).append(" ✓ (Have)\n");
            } else {
                missingCount++;
                ingBuilder.append("• ").append(req.getName()).append(": ").append(req.getRequiredQuantity()).append(" ").append(req.getUnit()).append(" ❌ (Missing)\n");
            }
        }

        if (missingCount == 0) {
            textStatus.setText("✅ Ready to cook!");
            textStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            textStatus.setText("❌ Missing " + missingCount + " ingredient(s)");
            textStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        textIngredients.setText(ingBuilder.toString().trim());
        textInstructions.setText(recipe.getInstructions());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
