package com.pes.androidrecipenest

import kotlin.text.category
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import java.util.Calendar

@Composable
fun DietMarkIcon(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(14.dp)
            .border(1.5.dp, color, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun DietSwitchButton(
    dietFilter: DietFilter,
    onToggle: () -> Unit
) {
    val isVeg = dietFilter == DietFilter.VEG
    val vegGreen = colorResource(R.color.veg_green)
    val nonVegRed = colorResource(R.color.non_veg_red)

    val accentColor by animateColorAsState(
        targetValue = if (isVeg) vegGreen else nonVegRed,
        label = "dietAccentColor"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isVeg) vegGreen.copy(alpha = 0.15f) else nonVegRed.copy(alpha = 0.15f),
        label = "dietContainerColor"
    )

    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.5.dp, accentColor)
    ) {
        AnimatedContent(
            targetState = isVeg,
            transitionSpec = {
                (slideInVertically { -it } + fadeIn()) togetherWith
                        (slideOutVertically { it } + fadeOut())
            },
            label = "dietSwitchContent"
        ) { vegState ->
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DietMarkIcon(color = if (vegState) vegGreen else nonVegRed)
                Text(
                    text = if (vegState) "Veg" else "Non-Veg",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (vegState) vegGreen else nonVegRed,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun ExploreScreen(
    viewModel: RecipeViewModel,
    onMealClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val dietFilter by viewModel.dietFilter.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    var query by remember { mutableStateOf(viewModel.savedLastQuery) }
    var searchByIngredient by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    val focusManager = LocalFocusManager.current

    val greeting = remember(userName) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeWord = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
        "$timeWord, $userName"
    }

    val cuisines = listOf("Indian", "Italian", "Mexican", "Chinese", "Thai")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.screen_padding))
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header Banner Card with App Logo, Personalized Greeting & Veg <-> Non-Veg Switch
        Surface(
            shape = RoundedCornerShape(dimensionResource(R.dimen.card_radius)),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = stringResource(R.string.app_name),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.header_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            viewModel.fetchRandomRecipe { randomId -> onMealClick(randomId) }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Surprise Recipe",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DietSwitchButton(
                        dietFilter = dietFilter,
                        onToggle = {
                            selectedFilter = "All"
                            viewModel.toggleDietFilter()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    if (searchByIngredient) "Search by ingredient (e.g., Paneer, Chicken)..."
                    else "Search delicious recipes..."
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        selectedFilter = "All"
                        viewModel.searchRecipes("", byIngredient = false)
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                viewModel.searchRecipes(query, searchByIngredient)
            })
        )

        // Room-backed Recent Search Chips
        if (recentSearches.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(top = 6.dp)
            ) {
                items(recentSearches, key = { it.query }) { item ->
                    AssistChip(
                        onClick = {
                            query = item.query
                            viewModel.searchRecipes(item.query, searchByIngredient)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        label = { Text(item.query, style = MaterialTheme.typography.labelSmall) }
                    )
                }
                item {
                    TextButton(
                        onClick = { viewModel.clearRecentSearches() },
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Search Mode Toggle
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !searchByIngredient,
                onClick = {
                    searchByIngredient = false
                    if (query.isNotBlank()) viewModel.searchRecipes(query, false)
                },
                label = { Text("By Recipe Name") }
            )
            FilterChip(
                selected = searchByIngredient,
                onClick = {
                    searchByIngredient = true
                    if (query.isNotBlank()) viewModel.searchRecipes(query, true)
                },
                label = { Text("By Ingredient") }
            )
        }

        // Combined Categories + World Cuisines Filter Strip
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedFilter == category,
                    onClick = {
                        selectedFilter = category
                        query = ""
                        viewModel.selectCategory(category)
                    },
                    label = { Text(category) }
                )
            }
            items(cuisines) { cuisine ->
                FilterChip(
                    selected = selectedFilter == cuisine,
                    onClick = {
                        selectedFilter = cuisine
                        query = ""
                        viewModel.selectCuisine(cuisine)
                    },
                    label = { Text("$cuisine Cuisine") }
                )
            }
        }

        // Smooth Crossfade between Loading, Error, and Recipe Grid
        Crossfade(targetState = uiState, label = "exploreState") { state ->
            when (state) {
                is RecipeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is RecipeUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.searchRecipes(query, searchByIngredient) }) {
                            Text("Retry")
                        }
                    }
                }
                is RecipeUiState.Success -> {
                    if (state.meals.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No recipes found. Try another search term.")
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.grid_spacing)),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.grid_spacing)),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.meals, key = { it.id }) { meal ->
                                val isSaved by viewModel.isMealSaved(meal.id).collectAsState(initial = false)
                                MealGridCard(
                                    meal = meal,
                                    isSaved = isSaved,
                                    onMealClick = { onMealClick(meal.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(meal, isSaved) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MealGridCard(
    meal: Meal,
    isSaved: Boolean,
    onMealClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val heartScale by animateFloatAsState(
        targetValue = if (isSaved) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "heartBounce"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onMealClick),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_radius)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            ) {
                GlideImage(
                    model = meal.imageUri,
                    contentDescription = meal.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.55f)
                                ),
                                startY = 120f
                            )
                        )
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = meal.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                ) {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save Recipe",
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(19.dp)
                                .graphicsLayer {
                                    scaleX = heartScale
                                    scaleY = heartScale
                                }
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}