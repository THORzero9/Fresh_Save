package com.bhaswat.freshsave.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhaswat.freshsave.model.InventoryItem
import com.bhaswat.freshsave.repository.InventoryRepository
import com.bhaswat.freshsave.model.RecipePlaceholder
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class HomeViewModel(private val inventoryRepository: InventoryRepository) : ViewModel() {

    private val _allItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val allItems: StateFlow<List<InventoryItem>> = _allItems.asStateFlow()

    private val _expiringSoonItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val expiringSoonItems: StateFlow<List<InventoryItem>> = _expiringSoonItems.asStateFlow()

    // New StateFlows for the new UI sections
    private val _totalItemsCount = MutableStateFlow(0)
    val totalItemsCount: StateFlow<Int> = _totalItemsCount.asStateFlow()

    private val _savingsValue = MutableStateFlow("$0.00") // Placeholder
    val savingsValue: StateFlow<String> = _savingsValue.asStateFlow()

    private val _itemCategories = MutableStateFlow<List<String>>(emptyList())
    val itemCategories: StateFlow<List<String>> = _itemCategories.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All") // Default to "All"
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Placeholder for a suggested recipe
    private val _suggestedRecipe = MutableStateFlow<RecipePlaceholder?>(null)
    val suggestedRecipe: StateFlow<RecipePlaceholder?> = _suggestedRecipe.asStateFlow()

    private val _itemToEdit = MutableStateFlow<InventoryItem?>(null)
    val itemToEdit: StateFlow<InventoryItem?> = _itemToEdit.asStateFlow()

    //--end of stateflows--

    init {
        Log.d("HomeViewModel" , "ViewModel initialized. Calling initial triggerLoadItems().")
        triggerLoadItems() // Call the new public trigger function
    }

    fun triggerLoadItems() {
        Log.d(
            "HomeViewModel" ,
            "triggerLoadItems called. Launching coroutine to execute suspend loadItems()."
        )
        viewModelScope.launch {
            loadItems()
        }
    }

    private suspend fun loadItems() {
        Log.d("HomeViewModel" , "loadItems (suspend fun): Starting.")
        var allItemsSuccess = false
        var expiringSoonSuccess = false
        try {
            Log.d("HomeViewModel" , "loadItems: Attempting to fetch all items.")
            inventoryRepository.getAllItems().onSuccess { items ->
                Log.d(
                    "HomeViewModel" ,
                    "loadItems: getAllItems successful. Fetched ${items.size} items."
                )
                withContext(NonCancellable) {
                    _allItems.value = items
                    _totalItemsCount.value =
                        items.size // <<-- CRITICAL: Update totalItemsCount HERE
                    // Also update categories if you have that feature
                    _itemCategories.value =
                        items.mapNotNull { it.category.takeIf { cat -> cat.isNotBlank() } }
                            .distinct()
                }
                Log.d(
                    "HomeViewModel" ,
                    "loadItems: _allItems & _totalItemsCount StateFlows updated. New allItems count: ${_allItems.value.size}, Total Items: ${_totalItemsCount.value}"
                )
                allItemsSuccess = true
            }.onFailure { exception ->
                Log.e(
                    "HomeViewModel" ,
                    "loadItems: getAllItems failed: ${exception.message}" ,
                    exception
                )
                withContext(NonCancellable) {
                    _allItems.value = emptyList()
                    _totalItemsCount.value = 0 // Reset on failure
                    _itemCategories.value = emptyList()
                }
            }

            Log.d("HomeViewModel" , "loadItems: Attempting to fetch expiring soon items.")
            inventoryRepository.getExpiringSoonItems().onSuccess { items ->
                Log.d(
                    "HomeViewModel" ,
                    "loadItems: getExpiringSoonItems successful. Fetched ${items.size} items."
                )
                withContext(NonCancellable) {
                    _expiringSoonItems.value = items
                }
                Log.d(
                    "HomeViewModel" ,
                    "loadItems: _expiringSoonItems StateFlow updated. New count: ${_expiringSoonItems.value.size}"
                )
                updateSuggestedRecipe(items)
                expiringSoonSuccess = true
            }.onFailure { exception ->
                Log.e(
                    "HomeViewModel" ,
                    "loadItems: getExpiringSoonItems failed: ${exception.message}" ,
                    exception
                )
                withContext(NonCancellable) { _expiringSoonItems.value = emptyList() }
            }
        } catch (e: CancellationException) {
            Log.w(
                "HomeViewModel" ,
                "loadItems (suspend fun) was cancelled. AllItemsUpdated: $allItemsSuccess, ExpiringSoonUpdated: $expiringSoonSuccess" ,
                e
            )
            throw e
        } catch (e: Exception) {
            Log.e("HomeViewModel" , "loadItems (suspend fun): Generic error: ${e.message}" , e)
            withContext(NonCancellable) {
                _allItems.value = emptyList()
                _expiringSoonItems.value = emptyList()
                _totalItemsCount.value = 0 // Reset on failure
                _itemCategories.value = emptyList()
                _suggestedRecipe.value = null
            }
        }
        Log.d(
            "HomeViewModel" ,
            "loadItems (suspend fun): Finished. AllItemsUpdated: $allItemsSuccess, ExpiringSoonUpdated: $expiringSoonSuccess"
        )
    }


    private fun updateSuggestedRecipe(expiringItems: List<InventoryItem>) {
        if (expiringItems.any { it.category.equals("Vegetables" , ignoreCase = true) }) {
            _suggestedRecipe.value = RecipePlaceholder(
                name = "Quick Garden Salad" ,
                description = "Use up those expiring vegetables!"
            )
        } else if (expiringItems.any { it.category.equals("Fruits" , ignoreCase = true) }) {
            _suggestedRecipe.value = RecipePlaceholder(
                name = "Fresh Fruit Smoothie" ,
                description = "Blend your expiring fruits."
            )
        } else {
            _suggestedRecipe.value = null
        }
    }


    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }


    fun addItem(item: InventoryItem) {
        viewModelScope.launch {
            inventoryRepository.addItem(item).onSuccess { newItemId ->
                Log.d(
                    "HomeViewModel" ,
                    "addItem: Item added successfully with ID: $newItemId. HomeScreen is responsible for triggering refresh."
                )
                // DO NOT call loadItems() or triggerLoadItems() here.
            }.onFailure { exception ->
                Log.e(
                    "HomeViewModel" ,
                    "addItem: Failed to add item: ${exception.message}" ,
                    exception
                )
                // TODO: Implement user-facing error message
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            inventoryRepository.deleteItem(itemId).onSuccess {
                Log.d(
                    "HomeViewModel" ,
                    "deleteItem: Item deleted successfully: $itemId. Calling triggerLoadItems()."
                )
                triggerLoadItems() // It's usually okay for delete to trigger refresh directly from VM
            }.onFailure { exception ->
                Log.e(
                    "HomeViewModel" ,
                    "deleteItem: Failed to delete item $itemId: ${exception.message}" ,
                    exception
                )
            }
        }
    }

    // Add other functions (e.g., updateItem) as needed
    fun loadItemForEditing(itemId: String) {
        viewModelScope.launch {
            inventoryRepository.getItem(itemId).onSuccess { item -> //
                _itemToEdit.value = item
                if (item == null) {
                    Log.w("HomeViewModel" , "loadItemForEditing: No item found with ID: $itemId")
                } else {
                    Log.d("HomeViewModel" , "loadItemForEditing: Item loaded: ${item.name}")
                }
            }.onFailure { exception ->
                _itemToEdit.value = null // Clear on failure
                Log.e(
                    "HomeViewModel" ,
                    "loadItemForEditing: Failed to get item $itemId: ${exception.message}" ,
                    exception
                )
                // TODO: User-facing error
            }
        }
    }

    fun clearItemToEdit() {
        _itemToEdit.value = null
        Log.d("HomeViewModel" , "clearItemToEdit: Item to edit cleared.")
    }

    fun updateItem(item: InventoryItem) {
        viewModelScope.launch {
            // Ensure the ID is present for an update
            if (item.id.isBlank()) {
                Log.e("HomeViewModel" , "updateItem: Item ID is blank, cannot update. Item: $item")
                // TODO: Handle this error case, perhaps show a message to the user
                return@launch
            }
            inventoryRepository.updateItem(item).onSuccess { //
                Log.d(
                    "HomeViewModel" ,
                    "updateItem: Item updated successfully: ${item.id}. HomeScreen will trigger refresh."
                )
                // Refresh is handled by HomeScreen observing SavedStateHandle (if we set it after update)
                // Or, if update is from AddItemScreen, popBackStack will lead to HomeScreen's LaunchedEffect
                // For direct updates from HomeScreen (like favorite toggle), we might call triggerLoadItems()
                // For now, assume AddItemScreen handles navigation and refresh signalling.
            }.onFailure { exception ->
                Log.e(
                    "HomeViewModel" ,
                    "updateItem: Failed to update item ${item.id}: ${exception.message}" ,
                    exception
                )
                // TODO: Implement user-facing error message
            }
        }
    }
        fun updateItemQuantity(itemId: String , newQuantity: Double) {
            viewModelScope.launch {
                // Get the current item to preserve other fields (like isFavorite, name, etc.)
                inventoryRepository.getItem(itemId).onSuccess { currentItem ->
                    if (currentItem != null) {
                        val updatedItem = currentItem.copy(quantity = newQuantity)
                        // Now call the general updateItem with the modified item
                        inventoryRepository.updateItem(updatedItem).onSuccess {
                            Log.d(
                                "HomeViewModel" ,
                                "Item quantity updated successfully for $itemId to $newQuantity via repository."
                            )
                            // Update local StateFlows for immediate UI reflection
                            _allItems.value = _allItems.value.map {
                                if (it.id == itemId) updatedItem else it
                            }
                            _expiringSoonItems.value = _expiringSoonItems.value.map {
                                if (it.id == itemId) updatedItem else it
                            }
                            // No need to call triggerLoadItems() if local update is sufficient
                            // and backend consistency is handled by this update.
                        }.onFailure { exception ->
                            Log.e(
                                "HomeViewModel" ,
                                "Failed to update item quantity in repository for $itemId: ${exception.message}" ,
                                exception
                            )
                            // TODO: User-facing error for repository update failure
                        }
                    } else {
                        Log.e(
                            "HomeViewModel" ,
                            "Item not found with ID: $itemId, cannot update quantity."
                        )
                        // TODO: User-facing error if item not found
                    }
                }.onFailure { exception ->
                    Log.e(
                        "HomeViewModel" ,
                        "Failed to get item $itemId before updating quantity: ${exception.message}" ,
                        exception
                    )
                    // TODO: User-facing error for initial fetch failure
                }
            }
        }
        // --- End New Function ---
    }

