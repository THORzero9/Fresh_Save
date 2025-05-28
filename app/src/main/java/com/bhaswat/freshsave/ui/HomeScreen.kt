package com.bhaswat.freshsave.ui

// import androidx.compose.foundation.layout.width // Not strictly needed for this update
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bhaswat.freshsave.R
import com.bhaswat.freshsave.model.InventoryItem
import com.bhaswat.freshsave.model.RecipePlaceholder
import com.bhaswat.freshsave.ui.navigation.Screen
import com.bhaswat.freshsave.utils.ITEM_ADDED_REFRESH_KEY_HOME
import com.bhaswat.freshsave.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
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

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<InventoryItem?>(null) }

    // Observe the result from AddItemScreen to trigger a refresh
    val currentBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(currentBackStackEntry) {
        if (currentBackStackEntry?.savedStateHandle?.remove<Boolean>(ITEM_ADDED_REFRESH_KEY_HOME) == true) {
            Log.d("HomeScreen", "Refresh triggered by ITEM_ADDED_REFRESH_KEY_HOME from AddItemScreen.")
            homeViewModel.triggerLoadItems()
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
            FloatingActionButton(onClick = { navController.navigate(Screen.AddItem.createRoute()) }) { // Updated to use createRoute
                Icon(Icons.Filled.Add, "Add new item")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) { // Wrap LazyColumn and Dialog in a Column
            if (showDeleteConfirmationDialog && itemToDelete != null) {
                DeleteConfirmationDialog(
                    itemName = itemToDelete!!.name,
                    onConfirm = {
                        homeViewModel.deleteItem(itemToDelete!!.id)
                        Toast.makeText(context, "${itemToDelete!!.name} deleted", Toast.LENGTH_SHORT).show()
                        showDeleteConfirmationDialog = false // Dismiss dialog
                        itemToDelete = null // Clear the item
                    },
                    onDismiss = {
                        showDeleteConfirmationDialog = false
                        itemToDelete = null
                    }
                )
            }

            LazyColumn(
                modifier = Modifier
                    // .padding(paddingValues) // Padding is now on the parent Column
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
                        key = { inventoryItem -> "expiring-${inventoryItem.id}" }
                    ) { item ->
                        InventoryItemCard(
                            item = item,
                            isExpiringSoon = true,
                            homeViewModel = homeViewModel,
                            navController = navController,
                            onDeleteActionInitiated = {
                                itemToDelete = it
                                showDeleteConfirmationDialog = true
                            }
                        )
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
                        key = { inventoryItem -> "all-${inventoryItem.id}" }
                    ) { item ->
                        InventoryItemCard(
                            item = item,
                            isExpiringSoon = false, // You might want to derive this based on date for "all items" too
                            homeViewModel = homeViewModel,
                            navController = navController,
                            onDeleteActionInitiated = {
                                itemToDelete = it
                                showDeleteConfirmationDialog = true
                            }
                        )
                    }
                } else {
                    item { Text(if (selectedCategory == "All") "No items in inventory yet." else "No items in this category.") }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Delete") },
        text = { Text("Are you sure you want to delete '$itemName'?") },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                // onDismiss() // onConfirm should handle dismissing if it's part of its logic
            }) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryItemCard(
    item: InventoryItem,
    isExpiringSoon: Boolean,
    homeViewModel: HomeViewModel,
    navController: NavController,
    onDeleteActionInitiated: (InventoryItem) -> Unit
) {
    //val coroutineScope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) { // Swiped Right to Left for Delete
                onDeleteActionInitiated(item)
                return@rememberSwipeToDismissBoxState false // Don't dismiss immediately, dialog handles it
            }
            false
        },
        positionalThreshold = { it * 0.4f } // Adjust swipe threshold if needed
    )


//    LaunchedEffect(dismissState.currentValue) {
//        // If swiped but no longer the target of a dialog (e.g., dialog dismissed)
//        // This is a heuristic. A better solution involves specific reset signals.
//        if (dismissState.targetValue != SwipeToDismissBoxValue.Settled &&
//            (navController.currentBackStackEntry?.destination?.route != Screen.AddItem.route)) { // Avoid reset if navigating for edit
//            // Complex to know if *this* item's dialog was dismissed.
//            // For now, we'll let the `confirmValueChange = false` handle not dismissing.
//            // The visual swipe will remain until next recomposition or user interaction.
//        }
//    }
    //---Quantity Adjustment Logic ---

    val quantityStep = 1.0
    val minQuantity =0.0
    val quantityFormat = remember { DecimalFormat("#,##0.##") } // Formats to max 2 decimal places

    fun adjustQuantity(item: InventoryItem, change: Double) {
        val newQuantity = item.quantity + change
        val clampedQuantity = newQuantity.coerceAtLeast(minQuantity)
        if (clampedQuantity != item.quantity) {
            homeViewModel.updateItemQuantity(item.id, clampedQuantity)
        }
    }
    // --- End Quantity Adjustment Logic ---


    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier, // Removed padding(vertical = 4.dp) - apply to Card or outer Box if needed per item
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                Color.Transparent
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp) // Added padding here for spacing between cards
                .clickable {
                    // For now, we are using a dedicated IconButton for Edit
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (item.expiryDate != null && item.expiryDate.before(Date())) {
                    MaterialTheme.colorScheme.error // Expired
                } else if (isExpiringSoon) {
                    MaterialTheme.colorScheme.errorContainer // Expiring soon
                } else {
                    MaterialTheme.colorScheme.surfaceVariant // Default
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp), // Adjust padding for icon
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleLarge)

                    // --- Quantity Display and Controls ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp) // Add some space above quantity controls
                    ) {
                        IconButton(
                            onClick = { adjustQuantity(item, -quantityStep) },
                            modifier = Modifier.size(36.dp), // Adjust size as needed
                            enabled = item.quantity > minQuantity // Disable if at or below min (if min > 0)
                                    // If minQuantity is 0, it's always enabled unless item.quantity is exactly 0.
                                    // Let's refine: disable if item.quantity - quantityStep < minQuantity to prevent going below on click
                                    // Simpler: disable if item.quantity <= minQuantity (if step would make it negative or zero if min is 0 and we want to stop at 0)
                                    // For now, let's allow it to reach 0. Button will be enabled if item.quantity > 0
                                    && (item.quantity - quantityStep >= minQuantity)

                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_remove_circle_24), // CHANGED to use imported drawable
                                contentDescription = "Decrease quantity",
                                tint = if (item.quantity - quantityStep >= minQuantity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }

                        Text(
                            // "${item.quantity} ${item.unit ?: ""}", // Original
                            text = "${quantityFormat.format(item.quantity)} ${item.unit ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp).width(IntrinsicSize.Min), // Give it some space
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            onClick = { adjustQuantity(item, quantityStep) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Outlined.AddCircle,
                                contentDescription = "Increase quantity",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    // --- End Quantity Display and Controls ---
//                    Text(
//                        "${item.quantity} ${item.unit ?: ""} (${item.category})",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
                    item.expiryDate?.let {
                        Text(
                            // "Expires on: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)}", // Original
                            "Expiry: ${SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(it)}", // Shortened for space
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    // Favorite Icon and Expiry Status Text moved below for better layout with Edit icon
                }
                IconButton(onClick = {
                    navController.navigate(Screen.AddItem.createRoute(item.id))
                }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Item", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        val updatedItem = item.copy(isFavorite = !item.isFavorite)
                        homeViewModel.updateItem(updatedItem) // Assumes updateItem handles this
                    }
                )
                Text(
                    text = "(${item.category})",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp) // Space from favorite icon
                )

                if (item.expiryDate != null && item.expiryDate.before(Date())) {
                    Text(
                        "EXPIRED!",
                        style = MaterialTheme.typography.labelSmall, // Adjusted for space
                        color = MaterialTheme.colorScheme.onError,
                    )
                } else if (isExpiringSoon) {
                    Text(
                        "EXPIRES SOON!",
                        style = MaterialTheme.typography.labelSmall, // Adjusted for space
                        color = MaterialTheme.colorScheme.error, // Text color on errorContainer
                    )
                }
            }
        }
    }
}


// SummarySection, CategoriesSection, RecipeSuggestionCard remain the same
// ... (rest of HomeScreen.kt: SummarySection, CategoriesSection, RecipeSuggestionCard)
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

@OptIn(ExperimentalMaterial3Api::class)
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