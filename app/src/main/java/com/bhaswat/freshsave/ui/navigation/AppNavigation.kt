package com.bhaswat.freshsave.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Home : Screen("home", "Inventory", Icons.Filled.Home)
    object Stats : Screen("stats", "Stats", Icons.Filled.DateRange) // Updated Icon
    object Recipes : Screen("recipes", "Recipes", Icons.Filled.ThumbUp) // Updated Icon
    object Donate : Screen("donate", "Donate", Icons.Filled.Share) // Updated Icon
    object AddItem : Screen("addItem", "Add New Item", icon = null)
}

val bottomNavigationItems = listOf(
    Screen.Home,
    Screen.Stats,
    Screen.Recipes,
    Screen.Donate
)