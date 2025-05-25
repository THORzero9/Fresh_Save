package com.bhaswat.freshsave.model

import java.util.Date


data class InventoryItem(
    val id: String = "" , // Optional: for database identification
    val name: String ,
    val category: String , // e.g., "Fruits", "Vegetables", "Dairy", etc.
    val quantity: Double = 1.0 , // Use Double for more flexibility (e.g., 1.5 kg)
    val unit: String? , // e.g., "kg", "liter", "piece", null if not applicable
    val expiryDate: Date? ,
    val isFavorite: Boolean = false // Default to not favorite
    // Add other relevant properties like image URL, etc.
)