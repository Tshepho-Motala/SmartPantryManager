package com.example.smartpantrymanager;

import java.io.Serializable;
import java.util.List;

public class Recipe implements Serializable {
    private long id;
    private String title;
    private String instructions;
    private List<RecipeIngredient> requiredIngredients;

    public static class RecipeIngredient implements Serializable {
        private String name;
        private double requiredQuantity;
        private String unit;

        public RecipeIngredient(String name, double requiredQuantity, String unit) {
            this.name = name;
            this.requiredQuantity = requiredQuantity;
            this.unit = unit;
        }

        public String getName() { return name; }
        public double getRequiredQuantity() { return requiredQuantity; }
        public String getUnit() { return unit; }
    }

    public Recipe(long id, String title, String instructions, List<RecipeIngredient> requiredIngredients) {
        this.id = id;
        this.title = title;
        this.instructions = instructions;
        this.requiredIngredients = requiredIngredients;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getInstructions() { return instructions; }
    public List<RecipeIngredient> getRequiredIngredients() { return requiredIngredients; }
}
