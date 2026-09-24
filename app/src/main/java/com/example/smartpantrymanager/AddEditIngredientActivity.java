package com.example.smartpantrymanager;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddEditIngredientActivity extends AppCompatActivity {

    private EditText etName, etQuantity, etUnit;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private long ingredientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        etName = findViewById(R.id.editItemName);
        etQuantity = findViewById(R.id.editItemQuantity);
        etUnit = findViewById(R.id.editItemUnit);
        btnSave = findViewById(R.id.btnSaveItem);

        if (getIntent().hasExtra("INGREDIENT_ID")) {
            ingredientId = getIntent().getLongExtra("INGREDIENT_ID", -1);
            setTitle("Edit Pantry Item");
            etName.setText(getIntent().getStringExtra("INGREDIENT_NAME"));
            etQuantity.setText(String.valueOf(getIntent().getDoubleExtra("INGREDIENT_QUANTITY", 0)));
            etUnit.setText(getIntent().getStringExtra("INGREDIENT_UNIT"));
        } else {
            setTitle("Add Pantry Item");
        }

        btnSave.setOnClickListener(v -> saveIngredient());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveIngredient() {
        String name = etName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Ingredient name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityStr);
            if (quantity <= 0) {
                etQuantity.setError("Quantity must be greater than zero");
                etQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid number");
            etQuantity.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(unit)) {
            etUnit.setError("Unit of measure is required");
            etUnit.requestFocus();
            return;
        }

        if (ingredientId != -1) {
            Ingredient ingredient = new Ingredient(ingredientId, name, quantity, unit);
            dbHelper.updatePantryItem(ingredient);
            Toast.makeText(this, "Ingredient Updated", Toast.LENGTH_SHORT).show();
        } else {
            Ingredient ingredient = new Ingredient(name, quantity, unit);
            dbHelper.addPantryItem(ingredient);
            Toast.makeText(this, "Ingredient Saved", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }
}
