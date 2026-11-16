package com.pes.androidrecipenest

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.delay
import kotlin.text.category
import kotlin.times

private fun scaleIngredientMeasure(measure: String, multiplier: Int): String {
    if (multiplier == 1 || measure.isBlank()) return measure
    val regex = Regex("""^(\d+(?:\.\d+)?)\s*(.*)$""")
    val match = regex.find(measure.trim()) ?: return "$measure (×$multiplier)"
    val number = match.groupValues[1].toDoubleOrNull() ?: return "$measure (×$multiplier)"
    val unit = match.groupValues[2]
    val scaled = number * multiplier
    val formatted = if (scaled % 1.0 == 0.0) scaled.toInt().toString() else String.format("%.1f", scaled)
    return if (unit.isNotBlank()) "$formatted $unit" else formatted
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun DetailScreen(
    mealId: String,
    viewModel: RecipeViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(mealId) {
        viewModel.loadMealDetail(mealId)
    }

    val meal by viewModel.selectedMeal.collectAsState()
    val isSaved by viewModel.isMealSaved(mealId).collectAsState(initial = false)
    val checkedIngredients = remember { mutableStateMapOf<Int, Boolean>() }

    // Interactive Serving Multiplier State
    var servingMultiplier by remember { mutableIntStateOf(1) }

    // Built-in Kitchen Countdown Timer State
    var timerSecondsLeft by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning, timerSecondsLeft) {
        if (isTimerRunning && timerSecondsLeft > 0) {
            delay(1000L)
            timerSecondsLeft -= 1
            if (timerSecondsLeft == 0) {
                isTimerRunning = false
                Toast.makeText(context, "Kitchen Timer Finished!", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Details", maxLines = 1, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    meal?.let { currentMeal ->
                        // Implicit Intent 1: Email Recipe
                        IconButton(onClick = {
                            val ingText = currentMeal.ingredients.joinToString("\n") { "• ${it.first}: ${it.second}" }
                            val body = "${currentMeal.name}\n\nIngredients:\n$ingText\n\nInstructions:\n${currentMeal.instructions}"
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_SUBJECT, "Recipe: ${currentMeal.name}")
                                putExtra(Intent.EXTRA_TEXT, body)
                            }
                            context.startActivity(Intent.createChooser(emailIntent, "Email Recipe"))
                        }) {
                            Icon(Icons.Default.Email, contentDescription = "Email Recipe")
                        }

                        // Implicit Intent 2: Share Recipe
                        IconButton(onClick = {
                            val ingText = currentMeal.ingredients.joinToString("\n") { "• ${it.first}: ${it.second}" }
                            val shareBody = "${currentMeal.name} (${currentMeal.area} • ${currentMeal.category})\n\nIngredients:\n$ingText\n\nInstructions:\n${currentMeal.instructions}"
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, currentMeal.name)
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Recipe via"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Recipe")
                        }

                        if (!currentMeal.isCustom) {
                            IconButton(onClick = { viewModel.toggleFavorite(currentMeal, isSaved) }) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isSaved) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        val currentMeal = meal
        if (currentMeal == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val dietColor = if (currentMeal.isVeg) {
                colorResource(R.color.veg_green)
            } else {
                colorResource(R.color.non_veg_red)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                if (currentMeal.imageUri.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        GlideImage(
                            model = currentMeal.imageUri,
                            contentDescription = currentMeal.name,
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
                                            MaterialTheme.colorScheme.background
                                        ),
                                        startY = 380f
                                    )
                                )
                        )
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DietMarkIcon(color = dietColor)
                        Text(
                            text = if (currentMeal.isVeg) "Vegetarian" else "Non-Vegetarian",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = dietColor
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentMeal.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Culinary Info Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(onClick = {}, label = { Text("${currentMeal.prepTimeMinutes} mins") })
                        SuggestionChip(onClick = {}, label = { Text(currentMeal.difficulty) })
                        SuggestionChip(onClick = {}, label = { Text(currentMeal.area) })
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons Row: Explicit Intent + Implicit Browser Intent
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val explicitIntent = Intent(context, CookingModeActivity::class.java).apply {
                                    putExtra(CookingModeActivity.EXTRA_MEAL_NAME, currentMeal.name)
                                    putExtra(CookingModeActivity.EXTRA_INSTRUCTIONS, currentMeal.instructions)
                                    putStringArrayListExtra(
                                        CookingModeActivity.EXTRA_INGREDIENTS,
                                        ArrayList(currentMeal.ingredients.map {
                                            "${it.first} (${scaleIngredientMeasure(it.second, servingMultiplier)})"
                                        })
                                    )
                                }
                                context.startActivity(explicitIntent)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cook Mode", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val targetUrl = if (currentMeal.youtubeUrl.isNotBlank()) {
                                    currentMeal.youtubeUrl
                                } else {
                                    "https://www.google.com/search?q=${Uri.encode(currentMeal.name + " recipe nutrition calories")}"
                                }
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                context.startActivity(browserIntent)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = if (currentMeal.youtubeUrl.isNotBlank()) Icons.Default.PlayArrow else Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentMeal.youtubeUrl.isNotBlank()) "Watch Video" else "Nutrition Web",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Built-in Kitchen Timer Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val mins = timerSecondsLeft / 60
                                    val secs = timerSecondsLeft % 60
                                    Text(
                                        text = if (timerSecondsLeft > 0)
                                            String.format("Kitchen Timer: %02d:%02d", mins, secs)
                                        else
                                            "Quick Kitchen Timer",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (timerSecondsLeft > 0) {
                                    Row {
                                        IconButton(onClick = { isTimerRunning = !isTimerRunning }) {
                                            Icon(
                                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Start/Pause Timer"
                                            )
                                        }
                                        IconButton(onClick = {
                                            isTimerRunning = false
                                            timerSecondsLeft = 0
                                        }) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Reset Timer")
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(5, 10, 15, 25).forEach { presetMin ->
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            timerSecondsLeft = presetMin * 60
                                            isTimerRunning = true
                                        },
                                        label = { Text("${presetMin}m") }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Ingredients Header + Serving Size Scaler
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ingredients (${currentMeal.ingredients.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1, 2, 3, 4).forEach { mult ->
                                FilterChip(
                                    selected = servingMultiplier == mult,
                                    onClick = { servingMultiplier = mult },
                                    label = {
                                        Text(
                                            text = "${mult}x",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "Scaled for ${currentMeal.servings * servingMultiplier} servings",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            currentMeal.ingredients.forEachIndexed { index, (ingredient, measure) ->
                                val isChecked = checkedIngredients[index] ?: false
                                val scaledMeasure = scaleIngredientMeasure(measure, servingMultiplier)
                                val textColor by animateColorAsState(
                                    targetValue = if (isChecked)
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    label = "ingredientTextColor"
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { checkedIngredients[index] = !isChecked }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checkedIngredients[index] = it }
                                    )
                                    Text(
                                        text = ingredient,
                                        color = textColor,
                                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = scaledMeasure,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChecked) textColor else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentMeal.instructions,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 26.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}