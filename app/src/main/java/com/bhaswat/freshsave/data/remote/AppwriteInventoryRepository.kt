package com.bhaswat.freshsave.data.remote


import android.util.Log
import androidx.compose.material3.DatePickerDefaults.dateFormatter
import com.bhaswat.freshsave.model.InventoryItem
import com.bhaswat.freshsave.repository.InventoryRepository
import io.appwrite.Client
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Databases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import java.util.TimeZone

class AppwriteInventoryRepository(appwriteClient: Client) : InventoryRepository {

    private val databases = Databases(appwriteClient)
    private val databaseId = "freshsave_db"
    private val collectionId = "67ef66ae0002c8940ee9"
    private val outputDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC") }
    private val TAG = "AppwriteRepo"

    override suspend fun addItem(item: InventoryItem): Result<String> = withContext(Dispatchers.IO) {
        // ... (addItem code - no logging added here for brevity)
        return@withContext try {
            val document = databases.createDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = "unique()",
                data = mapOf(
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "expiryDate" to item.expiryDate?.let { outputDateFormatter.format(it) }, // Format Date for Appwrite
                    "category" to item.category,
                    "unit" to item.unit,
                    "isFavorite" to item.isFavorite
                )
            )
            Result.success(document.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding item: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun parseAppwriteDate(dateString: String?): Date? {
        if (dateString.isNullOrEmpty()) return null
        return try {
            // Use XXX for parsing to handle offsets like +00:00 or Z
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            parser.parse(dateString)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date string '$dateString'", e)
            null
        }
    }

    override suspend fun getItem(itemId: String): Result<InventoryItem?> = withContext(Dispatchers.IO) {
        // ... (getItem code - no logging added here for brevity)
        return@withContext try {
            val document = databases.getDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = itemId
            )
            val data = document.data
            val inventoryItem = InventoryItem(
                id = document.id,
                name = data["name"] as? String ?: "",
                quantity = (data["quantity"] as? Number)?.toDouble() ?: 0.0,
                expiryDate = parseAppwriteDate(data["expiryDate"] as? String),
                category = data["category"] as? String ?: "",
                unit = data["unit"] as? String,
                isFavorite = data["isFavorite"] as? Boolean ?: false
            )
            Result.success(inventoryItem)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting item: ${e.message}", e)
            Result.success(null)
        }
    }

    override suspend fun updateItem(item: InventoryItem): Result<Unit> = withContext(Dispatchers.IO) {
        // ... (updateItem code - no logging added here for brevity)
        return@withContext try {
            databases.updateDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = item.id ,
                data = mapOf(
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "expiryDate" to item.expiryDate?.let { outputDateFormatter.format(it) }, // Format Date for Appwrite
                    "category" to item.category,
                    "unit" to item.unit,
                    "isFavorite" to item.isFavorite
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating item: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteItem(itemId: String): Result<Unit> = withContext(Dispatchers.IO) {
        // ... (deleteItem code - no logging added here for brevity)
        return@withContext try {
            databases.deleteDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = itemId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting item: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getAllItems(): Result<List<InventoryItem>> = withContext(Dispatchers.IO) { // Ensures all work is on IO
        return@withContext try {
            Log.d(TAG, "Fetching all items from Appwrite...")
            val response = databases.listDocuments(
                databaseId = databaseId,
                collectionId = collectionId
            )
            Log.d(TAG, "Fetched ${response.total} total documents from Appwrite for getAllItems.")

            val mappedItems = mutableListOf<InventoryItem>()
            response.documents.forEachIndexed { index, document ->
                // ... (your existing mapping logic using parseAppwriteDate) ...
                val inventoryItem = InventoryItem(
                    id = document.id,
                    name = document.data["name"] as? String ?: "",
                    category = document.data["category"] as? String ?: "",
                    quantity = (document.data["quantity"] as? Number)?.toDouble() ?: 0.0,
                    unit = document.data["unit"] as? String,
                    expiryDate = parseAppwriteDate(document.data["expiryDate"] as? String),
                    isFavorite = document.data["isFavorite"] as? Boolean ?: false
                )
                mappedItems.add(inventoryItem)
            }
            Log.d(TAG, "getAllItems - Finished mapping. Total successfully mapped items: ${mappedItems.size}")
            Result.success(mappedItems)
        } catch (e: AppwriteException) {
            Log.e(TAG, "AppwriteException in getAllItems: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Generic error in getAllItems: ${e.message}", e)
            Result.failure(e)
        }
    }
    override suspend fun getExpiringSoonItems(): Result<List<InventoryItem>> = withContext(Dispatchers.IO) {
        // This method already correctly uses withContext(Dispatchers.IO) for its entire body.
        // ... (implementation is correct from previous step) ...
        Log.d(TAG, "getExpiringSoonItems() called.")
        try {
            val calendar = Calendar.getInstance()
            val now = calendar.time
            val formattedCurrentDate = outputDateFormatter.format(now)

            calendar.add(Calendar.DAY_OF_YEAR, 7)
            val futureDate = calendar.time
            val formattedFutureDate = outputDateFormatter.format(futureDate)

            Log.d(TAG, "Querying for items expiring after $formattedCurrentDate AND on or before $formattedFutureDate")

            val queries = listOf(
                Query.greaterThan("expiryDate", formattedCurrentDate),
                Query.lessThanEqual("expiryDate", formattedFutureDate)
            )

            val response = databases.listDocuments(
                databaseId = databaseId,
                collectionId = collectionId,
                queries = queries
            )
            Log.d(TAG, "Fetched ${response.total} total documents from Appwrite for getExpiringSoonItems.")

            val mappedItems = mutableListOf<InventoryItem>()
            response.documents.forEachIndexed { index, document ->
                Log.d(TAG, "getExpiringSoonItems - Mapping document index $index, ID: ${document.id}, Data: ${document.data}")
                val inventoryItem = InventoryItem(
                    id = document.id,
                    name = document.data["name"] as? String ?: "",
                    category = document.data["category"] as? String ?: "",
                    quantity = (document.data["quantity"] as? Number)?.toDouble() ?: 0.0,
                    unit = document.data["unit"] as? String,
                    expiryDate = parseAppwriteDate(document.data["expiryDate"] as? String),
                    isFavorite = document.data["isFavorite"] as? Boolean ?: false
                )
                mappedItems.add(inventoryItem)
            }
            Log.d(TAG, "getExpiringSoonItems - Finished mapping. Total successfully mapped items: ${mappedItems.size}")
            Result.success(mappedItems)
        } catch (e: AppwriteException) {
            Log.e(TAG, "AppwriteException in getExpiringSoonItems: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Generic error in getExpiringSoonItems: ${e.message}", e)
            Result.failure(e)
        }
    }
}