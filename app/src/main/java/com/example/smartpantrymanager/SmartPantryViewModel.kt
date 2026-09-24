package com.example.smartpantrymanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SmartPantryViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)

    var pantryItems by mutableStateOf<List<PantryItem>>(emptyList())
        private set

    var recipes by mutableStateOf<List<Recipe>>(emptyList())
        private set

    var expiringSoonAlerts by mutableStateOf(true)
    var unitPreference by mutableStateOf("Metric (g, ml, pcs)")

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                dbHelper.getAllPantryItems()
            }
            val recs = withContext(Dispatchers.IO) {
                dbHelper.getAllRecipes()
            }
            pantryItems = items
            recipes = recs
        }
    }

    fun addItem(name: String, quantity: Double, unit: String, expiryDate: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val item = PantryItem(name.trim(), quantity, unit.trim(), expiryDate?.takeIf { it.isNotBlank() })
                dbHelper.addPantryItem(item)
            }
            loadData()
        }
    }

    fun updateItem(item: PantryItem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.updatePantryItem(item)
            }
            loadData()
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.deletePantryItem(id)
            }
            loadData()
        }
    }
}
