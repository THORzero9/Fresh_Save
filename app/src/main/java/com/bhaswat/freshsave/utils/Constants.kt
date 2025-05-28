package com.bhaswat.freshsave.utils

const val ITEM_ADDED_REFRESH_KEY_HOME = "item_added_refresh_key_home"
val PREDEFINED_UNITS = listOf("pcs", "kg", "g", "lbs", "oz", "liter", "ml", "Pack", "Dozen", "Can", "Bottle", "Box", "Jar", "Bag")
val PREDEFINED_CATEGORIES = listOf(
    "Fruits", "Vegetables", "Dairy & Eggs", "Meat", "Poultry", "Fish & Seafood",
    "Pantry Staples", "Grains & Pasta", "Cereals", "Bakery & Bread", "Frozen Foods",
    "Drinks & Beverages", "Snacks", "Condiments & Sauces", "Spices & Seasonings",
    "Sweets & Desserts", "Baby Food", "Pet Food", "Leftovers", "Other"
).sorted() // Keep them sorted for display