package com.example.smartpantrymanager;

import java.util.List;

public class Recipe {
    private int id;
    private String title;
    private String instructions;
    private List<RecipeIngredient> requiredIngredients;

    public static class RecipeIngredient {
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

    public Recipe(int id, String title, String instructions, List<RecipeIngredient> requiredIngredients) {
        this.id = id;
        this.title = title;
        this.instructions = instructions;
        this.requiredIngredients = requiredIngredients;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getInstructions() { return instructions; }
    public List<RecipeIngredient> getRequiredIngredients() { return requiredIngredients; }
}
