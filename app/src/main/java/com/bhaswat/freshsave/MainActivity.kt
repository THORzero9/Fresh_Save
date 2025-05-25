package com.bhaswat.freshsave

import android.R.attr.onClick
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bhaswat.freshsave.ui.DonateScreen
import com.bhaswat.freshsave.ui.HomeScreen
import com.bhaswat.freshsave.ui.RecipesScreen
import com.bhaswat.freshsave.ui.StatsScreen
import com.bhaswat.freshsave.ui.navigation.Screen
import com.bhaswat.freshsave.ui.navigation.bottomNavigationItems
import com.bhaswat.freshsave.ui.theme.FreshSaveTheme
import com.bhaswat.freshsave.ui.AddItemScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreshSaveTheme {
                MainAppScreen() // Call the new main screen composable
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavigationItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            // Handle the nullable ImageVector
                            screen.icon?.let { iconVector -> // Use ?.let for safety
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = screen.title
                                )
                            }
                               },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route, // Your home screen route
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Stats.route) { StatsScreen() } // Placeholder screen
            composable(Screen.Recipes.route) { RecipesScreen() } // Placeholder screen
            composable(Screen.Donate.route) { DonateScreen() }
            composable(Screen.AddItem.route) { AddItemScreen(navController) }
        }
    }
}