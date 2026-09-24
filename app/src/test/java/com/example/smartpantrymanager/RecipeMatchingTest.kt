package com.example.smartpantrymanager

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeMatchingTest {

    @Test
    fun testStrictMatchingRule_canMakeWhenAllIngredientsPresent() {
        val pantry = listOf(
            PantryItem("egg", 2.0, "pcs", null),
            PantryItem("butter", 15.0, "g", null),
            PantryItem("salt", 5.0, "tsp", null)
        )

        val ingredient = Recipe.RecipeIngredient("egg", 2.0, "pcs")
        val ingredient2 = Recipe.RecipeIngredient("butter", 10.0, "g")
        val recipe = Recipe(1, "Scrambled Eggs", "Cook", listOf(ingredient, ingredient2))

        val missingIngredients = recipe.requiredIngredients.filter { req ->
            val pantryMatch = pantry.find { it.name.equals(req.name, ignoreCase = true) }
            pantryMatch == null || pantryMatch.quantity < req.requiredQuantity
        }
        val canMake = missingIngredients.isEmpty()

        assertTrue("Should be able to make recipe when all ingredients are available in sufficient quantity", canMake)
    }

    @Test
    fun testStrictMatchingRule_cannotMakeWhenIngredientMissing() {
        val pantry = listOf(
            PantryItem("egg", 2.0, "pcs", null),
            // butter is missing!
            PantryItem("salt", 5.0, "tsp", null)
        )

        val ingredient = Recipe.RecipeIngredient("egg", 2.0, "pcs")
        val ingredient2 = Recipe.RecipeIngredient("butter", 10.0, "g")
        val recipe = Recipe(1, "Scrambled Eggs", "Cook", listOf(ingredient, ingredient2))

        val missingIngredients = recipe.requiredIngredients.filter { req ->
            val pantryMatch = pantry.find { it.name.equals(req.name, ignoreCase = true) }
            pantryMatch == null || pantryMatch.quantity < req.requiredQuantity
        }
        val canMake = missingIngredients.isEmpty()

        assertFalse("Should NOT be able to make recipe when an ingredient is missing", canMake)
    }

    @Test
    fun testStrictMatchingRule_cannotMakeWhenQuantityInsufficient() {
        val pantry = listOf(
            PantryItem("egg", 1.0, "pcs", null), // only 1, but 2 required
            PantryItem("butter", 10.0, "g", null),
            PantryItem("salt", 5.0, "tsp", null)
        )

        val ingredient = Recipe.RecipeIngredient("egg", 2.0, "pcs")
        val ingredient2 = Recipe.RecipeIngredient("butter", 10.0, "g")
        val recipe = Recipe(1, "Scrambled Eggs", "Cook", listOf(ingredient, ingredient2))

        val missingIngredients = recipe.requiredIngredients.filter { req ->
            val pantryMatch = pantry.find { it.name.equals(req.name, ignoreCase = true) }
            pantryMatch == null || pantryMatch.quantity < req.requiredQuantity
        }
        val canMake = missingIngredients.isEmpty()

        assertFalse("Should NOT be able to make recipe when ingredient quantity is insufficient", canMake)
    }
}
