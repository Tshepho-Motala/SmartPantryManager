package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry_manager.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_PANTRY = "pantry";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_EXPIRY = "expiry_date";

    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PANTRY_TABLE = "CREATE TABLE " + TABLE_PANTRY + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_QUANTITY + " REAL NOT NULL, "
                + COLUMN_UNIT + " TEXT NOT NULL, "
                + COLUMN_EXPIRY + " TEXT)";
        db.execSQL(CREATE_PANTRY_TABLE);

        String createRecipesTable = "CREATE TABLE " + TABLE_RECIPES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "instructions TEXT NOT NULL)";
        db.execSQL(createRecipesTable);

        String createRecipeIngredientsTable = "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER, " +
                "ingredient_name TEXT NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "FOREIGN KEY(recipe_id) REFERENCES " + TABLE_RECIPES + "(id))";
        db.execSQL(createRecipeIngredientsTable);

        seedRecipes(db);
        seedStarterPantry(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }

    // --- PANTRY CRUD ---
    public long addPantryItem(Ingredient ingredient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, ingredient.getName().trim().toLowerCase());
        values.put(COLUMN_QUANTITY, ingredient.getQuantity());
        values.put(COLUMN_UNIT, ingredient.getUnit().trim().toLowerCase());
        values.put(COLUMN_EXPIRY, ingredient.getExpiryDate());

        long id = db.insert(TABLE_PANTRY, null, values);
        db.close();
        return id;
    }

    public List<Ingredient> getAllPantryItems() {
        List<Ingredient> itemList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PANTRY + " ORDER BY " + COLUMN_NAME + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT));
                String expiry = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRY));

                Ingredient ing = new Ingredient(id, name, quantity, unit);
                ing.setExpiryDate(expiry);
                itemList.add(ing);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return itemList;
    }

    public int updatePantryItem(Ingredient ingredient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, ingredient.getName().trim().toLowerCase());
        values.put(COLUMN_QUANTITY, ingredient.getQuantity());
        values.put(COLUMN_UNIT, ingredient.getUnit().trim().toLowerCase());
        values.put(COLUMN_EXPIRY, ingredient.getExpiryDate());

        int rows = db.update(TABLE_PANTRY, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(ingredient.getId())});
        db.close();
        return rows;
    }

    public void deletePantryItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PANTRY, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // --- RECIPES ---
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPES, null);

        if (cursor.moveToFirst()) {
            do {
                long recipeId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String instructions = cursor.getString(cursor.getColumnIndexOrThrow("instructions"));

                List<Recipe.RecipeIngredient> ingredients = getRecipeIngredients(db, recipeId);
                recipes.add(new Recipe(recipeId, title, instructions, ingredients));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return recipes;
    }

    private List<Recipe.RecipeIngredient> getRecipeIngredients(SQLiteDatabase db, long recipeId) {
        List<Recipe.RecipeIngredient> ingredients = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPE_INGREDIENTS + " WHERE recipe_id = ?",
                new String[]{String.valueOf(recipeId)});

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("ingredient_name"));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
                ingredients.add(new Recipe.RecipeIngredient(name, qty, unit));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ingredients;
    }

    private void seedRecipes(SQLiteDatabase db) {
        insertRecipeWithIngredients(db, "Scrambled Eggs", "Beat eggs with salt. Melt butter on pan and gently scramble eggs until cooked.",
                new String[]{"egg", "butter", "salt"}, new double[]{2, 10, 1}, new String[]{"pcs", "g", "tsp"});

        insertRecipeWithIngredients(db, "French Toast", "Whisk egg and milk. Dip bread slices and fry in butter until golden brown.",
                new String[]{"egg", "milk", "bread", "butter"}, new double[]{1, 100, 2, 10}, new String[]{"pcs", "ml", "slices", "g"});

        insertRecipeWithIngredients(db, "Garlic Rice", "Heat oil, sauté minced garlic until golden, add cooked rice and stir-fry with salt.",
                new String[]{"rice", "garlic", "oil", "salt"}, new double[]{200, 2, 15, 1}, new String[]{"g", "cloves", "ml", "tsp"});

        insertRecipeWithIngredients(db, "Boiled Eggs", "Place eggs in boiling water for 8 minutes. Peel and serve with salt.",
                new String[]{"egg", "water", "salt"}, new double[]{2, 500, 1}, new String[]{"pcs", "ml", "tsp"});

        insertRecipeWithIngredients(db, "Simple Omelette", "Whisk eggs with salt and pepper. Pour into oiled pan, add cheese, fold and serve.",
                new String[]{"egg", "cheese", "oil", "salt"}, new double[]{2, 50, 10, 1}, new String[]{"pcs", "g", "ml", "tsp"});

        insertRecipeWithIngredients(db, "Tomato Pasta", "Boil pasta. Heat oil, sauté garlic and tomato paste, combine with pasta and salt.",
                new String[]{"pasta", "tomato paste", "garlic", "oil", "salt"}, new double[]{200, 50, 2, 15, 1}, new String[]{"g", "g", "cloves", "ml", "tsp"});

        insertRecipeWithIngredients(db, "Grilled Cheese Sandwich", "Butter bread slices, place cheese between slices, grill on medium heat until golden.",
                new String[]{"bread", "cheese", "butter"}, new double[]{2, 50, 10}, new String[]{"slices", "g", "g"});

        insertRecipeWithIngredients(db, "Pancakes", "Whisk flour, milk, egg, and sugar. Pour batter onto oiled pan and flip when bubbly.",
                new String[]{"flour", "milk", "egg", "sugar", "oil"}, new double[]{150, 200, 1, 20, 10}, new String[]{"g", "ml", "pcs", "g", "ml"});

        insertRecipeWithIngredients(db, "Mashed Potatoes", "Boil potatoes until tender. Mash with butter, milk, and salt until smooth.",
                new String[]{"potato", "butter", "milk", "salt"}, new double[]{3, 20, 50, 1}, new String[]{"pcs", "g", "ml", "tsp"});

        insertRecipeWithIngredients(db, "Butter Chicken Curry", "Sauté chicken in butter, stir in curry paste and cream. Simmer until tender.",
                new String[]{"chicken", "butter", "curry paste", "cream"}, new double[]{300, 20, 30, 100}, new String[]{"g", "g", "g", "ml"});

        insertRecipeWithIngredients(db, "Sautéed Spinach", "Heat oil, sauté garlic, add spinach leaves until wilted. Season with salt.",
                new String[]{"spinach", "garlic", "oil", "salt"}, new double[]{200, 2, 10, 1}, new String[]{"g", "cloves", "ml", "tsp"});

        insertRecipeWithIngredients(db, "Tuna Salad", "Mix canned tuna with mayonnaise, diced onion, and a pinch of salt.",
                new String[]{"tuna", "mayonnaise", "onion", "salt"}, new double[]{150, 30, 1, 1}, new String[]{"g", "g", "pcs", "tsp"});

        insertRecipeWithIngredients(db, "Oatmeal Bowl", "Boil oats in milk, stir continuously until thick. Add sugar or honey.",
                new String[]{"oats", "milk", "sugar"}, new double[]{50, 250, 10}, new String[]{"g", "ml", "g"});

        insertRecipeWithIngredients(db, "Garlic Toast", "Spread butter and minced garlic on bread. Toast in oven or pan.",
                new String[]{"bread", "butter", "garlic"}, new double[]{2, 15, 1}, new String[]{"slices", "g", "cloves"});

        insertRecipeWithIngredients(db, "Baked Potato", "Rub potato with oil and salt. Bake in oven at 200°C for 45 minutes.",
                new String[]{"potato", "oil", "salt"}, new double[]{2, 10, 1}, new String[]{"pcs", "ml", "tsp"});
    }

    private void insertRecipeWithIngredients(SQLiteDatabase db, String title, String instructions, String[] ingredients, double[] quantities, String[] units) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("instructions", instructions);
        long recipeId = db.insert(TABLE_RECIPES, null, values);

        for (int i = 0; i < ingredients.length; i++) {
            ContentValues ingValues = new ContentValues();
            ingValues.put("recipe_id", recipeId);
            ingValues.put("ingredient_name", ingredients[i].toLowerCase());
            ingValues.put("quantity", quantities[i]);
            ingValues.put("unit", units[i].toLowerCase());
            db.insert(TABLE_RECIPE_INGREDIENTS, null, ingValues);
        }
    }

    private void seedStarterPantry(SQLiteDatabase db) {
        String[][] starterItems = {
            {"egg", "6.0", "pcs", "2026-03-30"},
            {"butter", "100.0", "g", "2026-04-15"},
            {"salt", "50.0", "tsp", "2027-01-01"},
            {"bread", "10.0", "slices", "2026-03-12"},
            {"milk", "500.0", "ml", "2026-03-14"},
            {"rice", "500.0", "g", "2027-06-01"},
            {"garlic", "10.0", "cloves", "2026-03-25"},
            {"oil", "200.0", "ml", "2027-12-31"},
            {"cheese", "200.0", "g", "2026-03-20"},
            {"potato", "5.0", "pcs", "2026-03-22"}
        };

        for (String[] item : starterItems) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, item[0]);
            values.put(COLUMN_QUANTITY, Double.parseDouble(item[1]));
            values.put(COLUMN_UNIT, item[2]);
            values.put(COLUMN_EXPIRY, item[3]);
            db.insert(TABLE_PANTRY, null, values);
        }
    }
}
