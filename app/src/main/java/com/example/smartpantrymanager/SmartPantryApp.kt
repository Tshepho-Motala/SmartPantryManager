package com.example.smartpantrymanager

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpantrymanager.ui.theme.SmartPantryManagerTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

// --- ROBUST INGREDIENT MATCHING & STRICT-MATCHING UTILS ---

fun isIngredientMatch(reqName: String, pantryName: String): Boolean {
    val req = reqName.trim().lowercase()
    val pantry = pantryName.trim().lowercase()

    if (req == pantry) return true

    // Plural/singular handling ("egg" vs "eggs", "tomato" vs "tomatoes", "potato" vs "potatoes", etc.)
    if (pantry.endsWith("es") && pantry.substring(0, pantry.length - 2) == req) return true
    if (req.endsWith("es") && req.substring(0, req.length - 2) == pantry) return true

    if (pantry.endsWith("s") && pantry.substring(0, pantry.length - 1) == req) return true
    if (req.endsWith("s") && req.substring(0, req.length - 1) == pantry) return true

    if (pantry.endsWith("ies") && req.endsWith("y") && pantry.substring(0, pantry.length - 3) == req.substring(0, req.length - 1)) return true
    if (req.endsWith("ies") && pantry.endsWith("y") && req.substring(0, req.length - 3) == pantry.substring(0, req.length - 1)) return true

    return false
}

fun getMissingIngredients(recipe: Recipe, pantryItems: List<PantryItem>): List<Recipe.RecipeIngredient> {
    return recipe.requiredIngredients.filter { req ->
        val pantryMatch = pantryItems.find { pantryItem ->
            isIngredientMatch(req.name, pantryItem.name) && pantryItem.quantity >= req.requiredQuantity
        }
        pantryMatch == null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPantryApp(viewModel: SmartPantryViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<PantryItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRecipeDetail by remember { mutableStateOf<Recipe?>(null) }

    SmartPantryManagerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                0 -> "Smart Pantry Inventory"
                                1 -> "Suggested Recipes"
                                2 -> "All Recipes"
                                else -> "Settings"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Pantry") },
                        label = { Text("Pantry") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Suggested") },
                        label = { Text("Suggested") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Recipes") },
                        label = { Text("Recipes") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 }
                    )
                }
            },
            floatingActionButton = {
                if (selectedTab == 0) {
                    FloatingActionButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Pantry Item")
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (selectedTab) {
                    0 -> PantryScreen(
                        items = viewModel.pantryItems,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onEditItem = { itemToEdit = it },
                        onDeleteItem = { viewModel.deleteItem(it) }
                    )
                    1 -> SuggestedRecipesScreen(
                        recipes = viewModel.recipes,
                        pantryItems = viewModel.pantryItems,
                        onSelectRecipe = { selectedRecipeDetail = it }
                    )
                    2 -> AllRecipesScreen(
                        recipes = viewModel.recipes,
                        pantryItems = viewModel.pantryItems,
                        onSelectRecipe = { selectedRecipeDetail = it }
                    )
                    else -> SettingsScreen(
                        expiringAlerts = viewModel.expiringSoonAlerts,
                        onAlertsChanged = { viewModel.expiringSoonAlerts = it },
                        unitPref = viewModel.unitPreference,
                        onUnitPrefChanged = { viewModel.unitPreference = it }
                    )
                }
            }

            if (showAddDialog) {
                AddEditPantryItemDialog(
                    item = null,
                    onDismiss = { showAddDialog = false },
                    onSave = { name, qty, unit, expiry ->
                        viewModel.addItem(name, qty, unit, expiry)
                        showAddDialog = false
                    }
                )
            }

            itemToEdit?.let { item ->
                AddEditPantryItemDialog(
                    item = item,
                    onDismiss = { itemToEdit = null },
                    onSave = { name, qty, unit, expiry ->
                        item.name = name
                        item.quantity = qty
                        item.unit = unit
                        item.expiryDate = expiry
                        viewModel.updateItem(item)
                        itemToEdit = null
                    }
                )
            }

            selectedRecipeDetail?.let { recipe ->
                RecipeDetailDialog(
                    recipe = recipe,
                    pantryItems = viewModel.pantryItems,
                    onDismiss = { selectedRecipeDetail = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    items: List<PantryItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onEditItem: (PantryItem) -> Unit,
    onDeleteItem: (Int) -> Unit
) {
    val filteredItems = items.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search pantry items...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (items.isEmpty()) "Your pantry is empty.\nTap '+' to add items!" else "No matching items found.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredItems, key = { it.id }) { item ->
                    PantryItemCard(item = item, onEdit = { onEditItem(item) }, onDelete = { onDeleteItem(item.id) })
                }
            }
        }
    }
}

@Composable
fun PantryItemCard(item: PantryItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    val expiryStatus = getExpiryStatus(item.expiryDate)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (expiryStatus) {
                ExpiryStatus.EXPIRED -> MaterialTheme.colorScheme.errorContainer
                ExpiryStatus.EXPIRING_SOON -> MaterialTheme.colorScheme.tertiaryContainer
                ExpiryStatus.FRESH -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name.replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quantity: ${item.quantity} ${item.unit}",
                    fontSize = 14.sp
                )
                if (!item.expiryDate.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Expiry: ${item.expiryDate}",
                        fontSize = 12.sp,
                        color = when (expiryStatus) {
                            ExpiryStatus.EXPIRED -> MaterialTheme.colorScheme.onErrorContainer
                            ExpiryStatus.EXPIRING_SOON -> MaterialTheme.colorScheme.onTertiaryContainer
                            ExpiryStatus.FRESH -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

enum class ExpiryStatus { FRESH, EXPIRING_SOON, EXPIRED }

fun getExpiryStatus(expiryDate: String?): ExpiryStatus {
    if (expiryDate.isNullOrBlank()) return ExpiryStatus.FRESH
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.isLenient = false
        val expiry = sdf.parse(expiryDate) ?: return ExpiryStatus.FRESH
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val diffMillis = expiry.time - today.time
        val diffDays = diffMillis / (1000 * 60 * 60 * 24)

        return when {
            diffDays < 0 -> ExpiryStatus.EXPIRED
            diffDays <= 3 -> ExpiryStatus.EXPIRING_SOON
            else -> ExpiryStatus.FRESH
        }
    } catch (e: Exception) {
        return ExpiryStatus.FRESH
    }
}

@Composable
fun AddEditPantryItemDialog(
    item: PantryItem?,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String?) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var quantity by remember { mutableStateOf(item?.quantity?.toString() ?: "1.0") }
    var unit by remember { mutableStateOf(item?.unit ?: "pcs") }
    var expiryDate by remember { mutableStateOf(item?.expiryDate ?: "") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            expiryDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add Pantry Item" else "Edit Pantry Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (e.g., kg, g, pcs, ml)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = {},
                    label = { Text("Expiry Date (YYYY-MM-DD)") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qtyDouble = quantity.toDoubleOrNull() ?: 1.0
                    if (name.isNotBlank()) {
                        onSave(name, qtyDouble, unit, expiryDate.takeIf { it.isNotBlank() })
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SuggestedRecipesScreen(
    recipes: List<Recipe>,
    pantryItems: List<PantryItem>,
    onSelectRecipe: (Recipe) -> Unit
) {
    val strictReadyRecipes = recipes.filter { getMissingIngredients(it, pantryItems).isEmpty() }
    val almostThereRecipes = recipes.filter { getMissingIngredients(it, pantryItems).size == 1 }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (strictReadyRecipes.isEmpty() && almostThereRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Kitchen,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No recipes match your pantry yet - add more ingredients!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (strictReadyRecipes.isNotEmpty()) {
                    item {
                        Text(
                            text = "✅ Ready to Cook (Strict Matches):",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(strictReadyRecipes, key = { it.id }) { recipe ->
                        RecipeCardItem(recipe = recipe, pantryItems = pantryItems, onClick = { onSelectRecipe(recipe) })
                    }
                }

                if (almostThereRecipes.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ Almost There (Missing 1 Ingredient - Bonus):",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    items(almostThereRecipes, key = { it.id }) { recipe ->
                        RecipeCardItem(recipe = recipe, pantryItems = pantryItems, onClick = { onSelectRecipe(recipe) })
                    }
                }
            }
        }
    }
}

@Composable
fun AllRecipesScreen(
    recipes: List<Recipe>,
    pantryItems: List<PantryItem>,
    onSelectRecipe: (Recipe) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = recipes.filter { it.title.contains(query, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search recipe collection...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matching recipes found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { recipe ->
                    RecipeCardItem(recipe = recipe, pantryItems = pantryItems, onClick = { onSelectRecipe(recipe) })
                }
            }
        }
    }
}

@Composable
fun RecipeCardItem(recipe: Recipe, pantryItems: List<PantryItem>, onClick: () -> Unit) {
    val missingIngredients = getMissingIngredients(recipe, pantryItems)
    val canMake = missingIngredients.isEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                canMake -> MaterialTheme.colorScheme.primaryContainer
                missingIngredients.size == 1 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            canMake -> "✅ Ready to cook!"
                            missingIngredients.size == 1 -> "⚠️ Almost there! Missing: ${missingIngredients.first().name}"
                            else -> "❌ Missing ${missingIngredients.size} ingredient(s)"
                        },
                        fontSize = 14.sp,
                        color = when {
                            canMake -> MaterialTheme.colorScheme.primary
                            missingIngredients.size == 1 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RecipeDetailDialog(
    recipe: Recipe,
    pantryItems: List<PantryItem>,
    onDismiss: () -> Unit
) {
    val missingIngredients = getMissingIngredients(recipe, pantryItems)
    val canMake = missingIngredients.isEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = recipe.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        canMake -> "✅ Ready to cook!"
                        missingIngredients.size == 1 -> "⚠️ Almost there! Missing: ${missingIngredients.first().name}"
                        else -> "❌ Missing ${missingIngredients.size} ingredient(s)"
                    },
                    fontSize = 13.sp,
                    color = when {
                        canMake -> MaterialTheme.colorScheme.primary
                        missingIngredients.size == 1 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(text = "Required Ingredients:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                items(recipe.requiredIngredients) { req ->
                    val pantryMatch = pantryItems.find { pantryItem ->
                        isIngredientMatch(req.name, pantryItem.name) && pantryItem.quantity >= req.requiredQuantity
                    }
                    val hasEnough = pantryMatch != null
                    Text(
                        text = "• ${req.name.replaceFirstChar { it.uppercase() }}: ${req.requiredQuantity} ${req.unit} ${if (hasEnough) "✓ (Have)" else "❌ (Missing / Insufficient)"}",
                        fontSize = 14.sp,
                        color = if (hasEnough) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Instructions:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = recipe.instructions, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SettingsScreen(
    expiringAlerts: Boolean,
    onAlertsChanged: (Boolean) -> Unit,
    unitPref: String,
    onUnitPrefChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "App Settings & Preferences",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Expiring Soon Alerts", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Highlight items expiring within 3 days", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = expiringAlerts, onCheckedChange = onAlertsChanged)
                }

                HorizontalDivider()

                Column {
                    Text(text = "Unit Preference", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = unitPref.contains("Metric"),
                            onClick = { onUnitPrefChanged("Metric (g, ml, pcs)") },
                            label = { Text("Metric") }
                        )
                        FilterChip(
                            selected = unitPref.contains("Imperial"),
                            onClick = { onUnitPrefChanged("Imperial (oz, fl oz, pcs)") },
                            label = { Text("Imperial") }
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "About Smart Pantry Manager", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Version 1.0 (Mobile App Development 700)", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Helps reduce food waste by tracking your home pantry and strictly matching recipes you can cook right now.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
