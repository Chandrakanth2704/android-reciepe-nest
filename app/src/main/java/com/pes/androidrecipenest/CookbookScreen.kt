package com.pes.androidrecipenest

import androidx.compose.foundation.layout.size

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage


@Composable
fun CookbookScreen(
    viewModel: RecipeViewModel,
    onMealClick: (String) -> Unit,
    onAddRecipeClick: () -> Unit,
    onEditRecipeClick: (String) -> Unit
) {
    val favorites by viewModel.favoriteRecipes.collectAsState()
    val personalRecipes by viewModel.personalRecipes.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var cookbookSearch by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddRecipeClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Recipe") },
                text = { Text("New Recipe", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "My Cookbook",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Instant Filter Bar inside Cookbook
            OutlinedTextField(
                value = cookbookSearch,
                onValueChange = { cookbookSearch = it },
                placeholder = { Text("Filter your cookbook by dish or category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (cookbookSearch.isNotEmpty()) {
                        IconButton(onClick = { cookbookSearch = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Saved Favorites (${favorites.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("My Recipes (${personalRecipes.size})") }
                )
            }

            AnimatedContent(targetState = selectedTab, label = "cookbookTabTransition") { tabIndex ->
                val rawList = if (tabIndex == 0) favorites else personalRecipes
                val displayedList = remember(rawList, cookbookSearch) {
                    if (cookbookSearch.isBlank()) rawList
                    else rawList.filter {
                        it.name.contains(cookbookSearch, ignoreCase = true) ||
                                it.category.contains(cookbookSearch, ignoreCase = true)
                    }
                }

                if (displayedList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                cookbookSearch.isNotBlank() -> "No matching recipes in your cookbook."
                                tabIndex == 0 -> "No saved favorites yet. Tap the heart icon on any recipe to save it offline."
                                else -> "No personal recipes yet. Tap '+ New Recipe' to craft your own dish."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayedList, key = { it.id }) { meal ->
                            SavedMealRowCard(
                                meal = meal,
                                onClick = { onMealClick(meal.id) },
                                onEdit = if (meal.isCustom) { { onEditRecipeClick(meal.id) } } else null,
                                onDelete = { viewModel.deleteRecipe(meal) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SavedMealRowCard(
    meal: Meal,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: () -> Unit
) {
    val dietColor = if (meal.isVeg) colorResource(R.color.veg_green) else colorResource(R.color.non_veg_red)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (meal.imageUri.isNotBlank()) {
                GlideImage(
                    model = meal.imageUri,
                    contentDescription = meal.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.RestaurantMenu, contentDescription = null)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DietMarkIcon(color = dietColor)
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${meal.category} • ${meal.prepTimeMinutes}m • ${meal.difficulty}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${meal.ingredients.size} ingredients • ${meal.servings} servings",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Recipe",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Recipe",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}