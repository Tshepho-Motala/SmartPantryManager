package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private List<Recipe> recipeList;
    private List<Ingredient> pantryItems;
    private OnRecipeClickListener listener;

    public RecipeAdapter(List<Recipe> recipeList, List<Ingredient> pantryItems, OnRecipeClickListener listener) {
        this.recipeList = recipeList;
        this.pantryItems = pantryItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.bind(recipe, pantryItems, listener);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, statusTextView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textRecipeTitle);
            statusTextView = itemView.findViewById(R.id.textRecipeStatus);
        }

        public void bind(final Recipe recipe, List<Ingredient> pantryItems, final OnRecipeClickListener listener) {
            titleTextView.setText(recipe.getTitle());

            int missingCount = 0;
            String missingName = "";
            for (Recipe.RecipeIngredient req : recipe.getRequiredIngredients()) {
                boolean found = false;
                for (Ingredient p : pantryItems) {
                    if (isIngredientMatch(req.getName(), p.getName()) && p.getQuantity() >= req.getRequiredQuantity()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    missingCount++;
                    missingName = req.getName();
                }
            }

            if (missingCount == 0) {
                statusTextView.setText("✅ Ready to cook!");
                statusTextView.setTextColor(itemView.getResources().getColor(android.R.color.holo_green_dark));
            } else if (missingCount == 1) {
                statusTextView.setText("⚠️ Almost there! Missing: " + missingName);
                statusTextView.setTextColor(itemView.getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                statusTextView.setText("❌ Missing " + missingCount + " ingredient(s)");
                statusTextView.setTextColor(itemView.getResources().getColor(android.R.color.holo_red_dark));
            }

            itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
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
}
