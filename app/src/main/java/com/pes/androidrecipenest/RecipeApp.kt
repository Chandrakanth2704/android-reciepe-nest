package com.pes.androidrecipenest

import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun RecipeApp(
    incomingSharedText: String? = null,
    viewModel: RecipeViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(incomingSharedText) {
        if (!incomingSharedText.isNullOrBlank()) {
            val encoded = Uri.encode(incomingSharedText)
            navController.navigate("add_recipe?sharedText=$encoded")
        }
    }

    val topLevelRoutes = listOf("explore", "cookbook", "profile")
    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "explore",
                        onClick = {
                            navController.navigate("explore") {
                                popUpTo("explore") { inclusive = true }
                            }
                        },
                        icon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.tab_explore)) },
                        label = { Text(stringResource(R.string.tab_explore), maxLines = 1, softWrap = false) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "cookbook",
                        onClick = {
                            navController.navigate("cookbook") {
                                popUpTo("explore")
                            }
                        },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = stringResource(R.string.tab_cookbook)) },
                        label = { Text(stringResource(R.string.tab_cookbook), maxLines = 1, softWrap = false) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "profile",
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo("explore")
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.tab_profile)) },
                        label = { Text(stringResource(R.string.tab_profile), maxLines = 1, softWrap = false) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "explore",
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("explore") {
                ExploreScreen(
                    viewModel = viewModel,
                    onMealClick = { mealId -> navController.navigate("detail/$mealId") }
                )
            }
            composable("cookbook") {
                CookbookScreen(
                    viewModel = viewModel,
                    onMealClick = { mealId -> navController.navigate("detail/$mealId") },
                    onAddRecipeClick = { navController.navigate("add_recipe") },
                    onEditRecipeClick = { mealId -> navController.navigate("add_recipe?editId=$mealId") }
                )
            }
            composable("profile") {
                ProfileScreen(viewModel = viewModel)
            }
            composable(
                route = "detail/{mealId}",
                arguments = listOf(navArgument("mealId") { type = NavType.StringType })
            ) { backStackEntry ->
                val mealId = backStackEntry.arguments?.getString("mealId").orEmpty()
                DetailScreen(
                    mealId = mealId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "add_recipe?editId={editId}&sharedText={sharedText}",
                arguments = listOf(
                    navArgument("editId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("sharedText") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val editId = backStackEntry.arguments?.getString("editId")
                val sharedText = backStackEntry.arguments?.getString("sharedText")
                AddRecipeScreen(
                    viewModel = viewModel,
                    editMealId = editId,
                    sharedText = sharedText,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}