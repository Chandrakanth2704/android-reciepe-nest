package com.pes.recipefinderandcookbook

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import java.io.File
import kotlin.math.roundToInt

private fun createCameraImageUri(context: Context): Uri {
    val imagesDir = File(context.filesDir, "recipe_images").apply { mkdirs() }
    val imageFile = File(imagesDir, "dish_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun AddRecipeScreen(
    viewModel: RecipeViewModel,
    editMealId: String? = null,
    sharedText: String? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Vegetarian") }
    var area by remember { mutableStateOf("Indian") }
    var isVeg by remember { mutableStateOf(true) }
    var prepTimeMinutes by remember { mutableFloatStateOf(30f) }
    var difficulty by remember { mutableStateOf("Medium") }
    var servings by remember { mutableIntStateOf(2) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val ingredients = remember { mutableStateListOf("" to "") }
    val cookingSteps = remember { mutableStateListOf("") }

    val difficultyOptions = listOf("Easy", "Medium", "Chef Special")
    val quickUnits = listOf("tsp", "tbsp", "cup", "g", "ml", "pcs")
    val vegGreen = colorResource(R.color.veg_green)
    val nonVegRed = colorResource(R.color.non_veg_red)

    LaunchedEffect(editMealId, sharedText) {
        if (!editMealId.isNullOrBlank()) {
            viewModel.getMealForEdit(editMealId)?.let { existing ->
                name = existing.name
                category = existing.category
                area = existing.area
                isVeg = existing.isVeg
                prepTimeMinutes = existing.prepTimeMinutes.toFloat()
                difficulty = existing.difficulty
                servings = existing.servings
                if (existing.imageUri.isNotBlank()) {
                    selectedImageUri = Uri.parse(existing.imageUri)
                }
                ingredients.clear()
                if (existing.ingredients.isNotEmpty()) {
                    ingredients.addAll(existing.ingredients)
                } else {
                    ingredients.add("" to "")
                }

                cookingSteps.clear()
                val parsedSteps = existing.instructions
                    .split("\n")
                    .map { it.replace(Regex("""^\d+\.\s*"""), "").trim() }
                    .filter { it.isNotBlank() }
                if (parsedSteps.isNotEmpty()) {
                    cookingSteps.addAll(parsedSteps)
                } else {
                    cookingSteps.add("")
                }
            }
        } else if (!sharedText.isNullOrBlank()) {
            cookingSteps.clear()
            cookingSteps.addAll(
                sharedText.split("\n").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { listOf(sharedText) }
            )
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            selectedImageUri = pendingCameraUri
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { }
            selectedImageUri = it
        }
    }

    val hasValidInstructions = cookingSteps.any { it.isNotBlank() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editMealId != null) "Edit Personal Recipe" else "Craft New Recipe",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Dish Photo Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    GlideImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Dish",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(38.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Snap or upload your dish photo", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val uri = createCameraImageUri(context)
                        pendingCameraUri = uri
                        cameraLauncher.launch(uri)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Take Photo")
                }

                OutlinedButton(
                    onClick = { galleryLauncher.launch(arrayOf("image/*")) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gallery")
                }
            }

            // 2. Dish Title & Veg/Non-Veg Dietary Type Selector
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Recipe Name *") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = {
                        isVeg = true
                        if (category == "Non-Veg") category = "Vegetarian"
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isVeg) vegGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, if (isVeg) vegGreen else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DietMarkIcon(color = vegGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pure Veg", fontWeight = FontWeight.Bold, color = vegGreen)
                    }
                }

                Surface(
                    onClick = {
                        isVeg = false
                        if (category == "Vegetarian") category = "Non-Veg"
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (!isVeg) nonVegRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, if (!isVeg) nonVegRed else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DietMarkIcon(color = nonVegRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Non-Veg", fontWeight = FontWeight.Bold, color = nonVegRed)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Cuisine") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. Culinary Details Card (Prep Time Slider, Servings Stepper & Difficulty)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Prep & Cook Time: ${prepTimeMinutes.roundToInt()} mins",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Servings Counter
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(
                                onClick = { if (servings > 1) servings-- },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease Servings", modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = " $servings Serv ",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            FilledTonalIconButton(
                                onClick = { if (servings < 20) servings++ },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase Servings", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Slider(
                        value = prepTimeMinutes,
                        onValueChange = { prepTimeMinutes = it },
                        valueRange = 10f..120f,
                        steps = 21
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        difficultyOptions.forEach { level ->
                            FilterChip(
                                selected = difficulty == level,
                                onClick = { difficulty = level },
                                label = { Text(level) }
                            )
                        }
                    }
                }
            }

            // 4. Ingredients Section with Quick Measurement Unit Chips
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(quickUnits) { unit ->
                    SuggestionChip(
                        onClick = {
                            val lastIdx = ingredients.lastIndex
                            if (lastIdx >= 0) {
                                val current = ingredients[lastIdx]
                                val updatedQty = if (current.second.isBlank()) "1 $unit" else "${current.second.trim()} $unit"
                                ingredients[lastIdx] = current.first to updatedQty
                            }
                        },
                        label = { Text("+ $unit", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            ingredients.forEachIndexed { index, pair ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pair.first,
                        onValueChange = { ingredients[index] = it to pair.second },
                        placeholder = { Text("Ingredient") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = pair.second,
                        onValueChange = { ingredients[index] = pair.first to it },
                        placeholder = { Text("Qty") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.9f)
                    )
                    if (ingredients.size > 1) {
                        IconButton(onClick = { ingredients.removeAt(index) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove")
                        }
                    }
                }
            }

            TextButton(onClick = { ingredients.add("" to "") }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Ingredient")
            }

            // 5. Step-by-Step Cooking Instructions Builder
            Text(
                text = "Step-by-Step Instructions *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            cookingSteps.forEachIndexed { stepIndex, stepText ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(
                            text = "${stepIndex + 1}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = stepText,
                        onValueChange = { cookingSteps[stepIndex] = it },
                        placeholder = { Text("Describe step ${stepIndex + 1}...") },
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    if (cookingSteps.size > 1) {
                        IconButton(
                            onClick = { cookingSteps.removeAt(stepIndex) },
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Step")
                        }
                    }
                }
            }

            TextButton(onClick = { cookingSteps.add("") }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Cooking Step")
            }

            Button(
                onClick = {
                    val formattedInstructions = cookingSteps
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .mapIndexed { idx, line -> "${idx + 1}. $line" }
                        .joinToString("\n\n")

                    viewModel.saveOrUpdatePersonalRecipe(
                        existingId = editMealId,
                        name = name,
                        category = category,
                        area = area,
                        instructions = formattedInstructions,
                        imageUri = selectedImageUri?.toString().orEmpty(),
                        ingredients = ingredients.toList(),
                        prepTimeMinutes = prepTimeMinutes.roundToInt(),
                        difficulty = difficulty,
                        servings = servings,
                        isVeg = isVeg
                    )
                    onBack()
                },
                enabled = name.isNotBlank() && hasValidInstructions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (editMealId != null) "Update Recipe" else "Save to My Cookbook",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}