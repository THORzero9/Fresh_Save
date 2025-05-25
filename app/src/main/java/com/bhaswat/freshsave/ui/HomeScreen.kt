package com.bhaswat.freshsave.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bhaswat.freshsave.model.InventoryItem
import com.bhaswat.freshsave.model.RecipePlaceholder
import com.bhaswat.freshsave.ui.navigation.Screen
import com.bhaswat.freshsave.utils.ITEM_ADDED_REFRESH_KEY_HOME
import com.bhaswat.freshsave.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

import com.bhaswat.freshsave.utils.ITEM_ADDED_REFRESH_KEY_HOME

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController, // HomeScreen now needs NavController
    homeViewModel: HomeViewModel = koinViewModel()
) {
    val allItemsList by homeViewModel.allItems.collectAsState()
    val expiringSoonItemsList by homeViewModel.expiringSoonItems.collectAsState()
    val totalItemsCount by homeViewModel.totalItemsCount.collectAsState()
    val savings by homeViewModel.savingsValue.collectAsState()
    val categories by homeViewModel.itemCategories.collectAsState()
    val selectedCategory by homeViewModel.selectedCategory.collectAsState()
    val suggestedRecipe by homeViewModel.suggestedRecipe.collectAsState()
    val context = LocalContext.current

    // Observe the result from AddItemScreen to trigger a refresh
    val currentBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(currentBackStackEntry) {
        if (currentBackStackEntry?.savedStateHandle?.remove<Boolean>(ITEM_ADDED_REFRESH_KEY_HOME) == true) {
            Log.d("HomeScreen", "Refresh triggered by ITEM_ADDED_REFRESH_KEY_HOME from AddItemScreen.")
            homeViewModel.triggerLoadItems() // Call the public trigger function in ViewModel
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FreshSave") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { Toast.makeText(context, "Search clicked", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Filled.Search, "Search", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    IconButton(onClick = { Toast.makeText(context, "Notifications clicked", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Filled.Notifications, "Notifications", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    IconButton(onClick = { Toast.makeText(context, "Account clicked", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Filled.AccountCircle, "Account", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddItem.route) }) {
                Icon(Icons.Filled.Add, "Add new item")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // 1. Summary Section
            item {
                SummarySection(
                    totalItems = totalItemsCount,
                    expiringSoonCount = expiringSoonItemsList.size,
                    savings = savings
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. Categories Section
            item {
                CategoriesSection(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category -> homeViewModel.onCategorySelected(category) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Optional Recipe Suggestion Card
            suggestedRecipe?.let { recipe ->
                item {
                    RecipeSuggestionCard(recipe = recipe)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 4. Expiring Soon Items Section
            item {
                Text("Expiring Soon", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 8.dp))
            }
            if (expiringSoonItemsList.isNotEmpty()) {
                items(
                    items = expiringSoonItemsList,
                    key = { inventoryItem -> "expiring-${inventoryItem.id}" } // Unique key
                ) { item ->
                    InventoryItemCard(item = item, isExpiringSoon = true)
                }
            } else {
                item { Text("No items expiring soon.") }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // 5. My Inventory Section (All Items / Filtered by Category)
            item {
                Text(
                    text = if (selectedCategory == "All") "My Inventory" else "My Inventory ($selectedCategory)",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            val itemsToDisplay = if (selectedCategory == "All") {
                allItemsList
            } else {
                allItemsList.filter { it.category == selectedCategory }
            }

            if (itemsToDisplay.isNotEmpty()) {
                items(
                    items = itemsToDisplay,
                    key = { inventoryItem -> "all-${inventoryItem.id}" } // Unique key
                ) { item ->
                    InventoryItemCard(item = item)
                }
            } else {
                item { Text(if (selectedCategory == "All") "No items in inventory yet." else "No items in this category.") }
            }
        }
    }
}

@Composable
fun SummarySection(totalItems: Int, expiringSoonCount: Int, savings: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryItem("Total Items", totalItems.toString())
            SummaryItem("Expiring Soon", expiringSoonCount.toString())
            SummaryItem("Savings", savings)
        }
    }
}

@Composable
fun SummaryItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall)
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun CategoriesSection(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Column {
        Text("Categories", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items = listOf("All") + categories, key = { category -> category }) { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) }
                )
            }
        }
    }
}

@Composable
fun RecipeSuggestionCard(recipe: RecipePlaceholder) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Try this recipe!", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(recipe.name, style = MaterialTheme.typography.titleMedium)
            Text(recipe.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun InventoryItemCard(item: InventoryItem, isExpiringSoon: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpiringSoon) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleLarge)
            Text("${item.quantity} ${item.unit ?: ""} (${item.category})", style = MaterialTheme.typography.bodyMedium)
            item.expiryDate?.let {
                Text("Expires on: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)}", style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.primary
                    // TODO: Add onClick to toggle favorite status
                )
            }
            if (isExpiringSoon) {
                Text("EXPIRES SOON!", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}