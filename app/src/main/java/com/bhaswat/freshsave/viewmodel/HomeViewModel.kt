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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class HomeViewModel(private val inventoryRepository: InventoryRepository) : ViewModel() {

    private val _allItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val allItems: StateFlow<List<InventoryItem>> = _allItems

    private val _expiringSoonItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val expiringSoonItems: StateFlow<List<InventoryItem>> = _expiringSoonItems

    // New StateFlows for the new UI sections
    private val _totalItemsCount = MutableStateFlow(0)
    val totalItemsCount: StateFlow<Int> = _totalItemsCount

    private val _savingsValue = MutableStateFlow("$0.00") // Placeholder
    val savingsValue: StateFlow<String> = _savingsValue

    private val _itemCategories = MutableStateFlow<List<String>>(emptyList())
    val itemCategories: StateFlow<List<String>> = _itemCategories

    private val _selectedCategory = MutableStateFlow("All") // Default to "All"
    val selectedCategory: StateFlow<String> = _selectedCategory

    // Placeholder for a suggested recipe
    private val _suggestedRecipe = MutableStateFlow<RecipePlaceholder?>(null)
    val suggestedRecipe: StateFlow<RecipePlaceholder?> = _suggestedRecipe

    init {
        Log.d("HomeViewModel", "ViewModel initialized. Calling initial triggerLoadItems().")
        triggerLoadItems() // Call the new public trigger function
    }
    fun triggerLoadItems() {
        Log.d("HomeViewModel", "triggerLoadItems called. Launching coroutine to execute suspend loadItems().")
        viewModelScope.launch {
            loadItems()
        }
    }
    private suspend fun loadItems() {
        Log.d("HomeViewModel", "loadItems (suspend fun): Starting.")
        var allItemsSuccess = false
        var expiringSoonSuccess = false
        try {
            Log.d("HomeViewModel", "loadItems: Attempting to fetch all items.")
            inventoryRepository.getAllItems().onSuccess { items ->
                Log.d("HomeViewModel", "loadItems: getAllItems successful. Fetched ${items.size} items.")
                withContext(NonCancellable) {
                    _allItems.value = items
                    _totalItemsCount.value = items.size // <<-- CRITICAL: Update totalItemsCount HERE
                    // Also update categories if you have that feature
                    _itemCategories.value = items.mapNotNull { it.category.takeIf { cat -> cat.isNotBlank() } }.distinct()
                }
                Log.d("HomeViewModel", "loadItems: _allItems & _totalItemsCount StateFlows updated. New allItems count: ${_allItems.value.size}, Total Items: ${_totalItemsCount.value}")
                allItemsSuccess = true
            }.onFailure { exception ->
                Log.e("HomeViewModel", "loadItems: getAllItems failed: ${exception.message}", exception)
                withContext(NonCancellable) {
                    _allItems.value = emptyList()
                    _totalItemsCount.value = 0 // Reset on failure
                    _itemCategories.value = emptyList()
                }
            }

            Log.d("HomeViewModel", "loadItems: Attempting to fetch expiring soon items.")
            inventoryRepository.getExpiringSoonItems().onSuccess { items ->
                Log.d("HomeViewModel", "loadItems: getExpiringSoonItems successful. Fetched ${items.size} items.")
                withContext(NonCancellable) {
                    _expiringSoonItems.value = items
                }
                Log.d("HomeViewModel", "loadItems: _expiringSoonItems StateFlow updated. New count: ${_expiringSoonItems.value.size}")
                updateSuggestedRecipe(items)
                expiringSoonSuccess = true
            }.onFailure { exception ->
                Log.e("HomeViewModel", "loadItems: getExpiringSoonItems failed: ${exception.message}", exception)
                withContext(NonCancellable) { _expiringSoonItems.value = emptyList() }
            }
        } catch (e: CancellationException) {
            Log.w("HomeViewModel", "loadItems (suspend fun) was cancelled. AllItemsUpdated: $allItemsSuccess, ExpiringSoonUpdated: $expiringSoonSuccess", e)
            throw e
        } catch (e: Exception) {
            Log.e("HomeViewModel", "loadItems (suspend fun): Generic error: ${e.message}", e)
            withContext(NonCancellable) {
                _allItems.value = emptyList()
                _expiringSoonItems.value = emptyList()
                _totalItemsCount.value = 0 // Reset on failure
                _itemCategories.value = emptyList()
                _suggestedRecipe.value = null
            }
        }
        Log.d("HomeViewModel", "loadItems (suspend fun): Finished. AllItemsUpdated: $allItemsSuccess, ExpiringSoonUpdated: $expiringSoonSuccess")
    }


    private fun updateSuggestedRecipe(expiringItems: List<InventoryItem>) {
        if (expiringItems.any { it.category.equals("Vegetables", ignoreCase = true) }) {
            _suggestedRecipe.value = RecipePlaceholder(name = "Quick Garden Salad", description = "Use up those expiring vegetables!")
        } else if (expiringItems.any { it.category.equals("Fruits", ignoreCase = true) }) {
            _suggestedRecipe.value = RecipePlaceholder(name = "Fresh Fruit Smoothie", description = "Blend your expiring fruits.")
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
                Log.d("HomeViewModel", "addItem: Item added successfully with ID: $newItemId. HomeScreen is responsible for triggering refresh.")
                // DO NOT call loadItems() or triggerLoadItems() here.
            }.onFailure { exception ->
                Log.e("HomeViewModel", "addItem: Failed to add item: ${exception.message}", exception)
                // TODO: Implement user-facing error message
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            inventoryRepository.deleteItem(itemId).onSuccess {
                Log.d("HomeViewModel", "deleteItem: Item deleted successfully: $itemId. Calling triggerLoadItems().")
                triggerLoadItems() // It's usually okay for delete to trigger refresh directly from VM
            }.onFailure { exception ->
                Log.e("HomeViewModel", "deleteItem: Failed to delete item $itemId: ${exception.message}", exception)
            }
        }
    }
    // Add other functions (e.g., updateItem) as needed
}