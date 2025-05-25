package com.bhaswat.freshsave.repository

import com.bhaswat.freshsave.model.InventoryItem

interface InventoryRepository {
    suspend fun addItem(item: InventoryItem): Result<String> // Returns the ID of the new item
    suspend fun getItem(itemId: String): Result<InventoryItem?>
    suspend fun updateItem(item: InventoryItem): Result<Unit>
    suspend fun deleteItem(itemId: String): Result<Unit>
    suspend fun getAllItems(): Result<List<InventoryItem>>
    suspend fun getExpiringSoonItems(): Result<List<InventoryItem>>
}