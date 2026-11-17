package com.pes.androidrecipenest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class CookingModeActivity : ComponentActivity() {

    companion object {
        const val EXTRA_MEAL_NAME = "extra_meal_name"
        const val EXTRA_INSTRUCTIONS = "extra_instructions"
        const val EXTRA_INGREDIENTS = "extra_ingredients"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extract data passed via Explicit Intent
        val mealName = intent.getStringExtra(EXTRA_MEAL_NAME) ?: "Cooking Mode"
        val rawInstructions = intent.getStringExtra(EXTRA_INSTRUCTIONS).orEmpty()
        val ingredients = intent.getStringArrayListExtra(EXTRA_INGREDIENTS) ?: arrayListOf()

        val steps = rawInstructions
            .split("\r\n", "\n", ". ")
            .map { it.trim().removeSuffix(".") }
            .filter { it.length > 3 }
            .ifEmpty { listOf(rawInstructions) }

        setContent {
            AndroidRecipeNestTheme {
                var currentStep by remember { mutableIntStateOf(0) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(mealName, maxLines = 1) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Exit Cooking Mode"
                                    )
                                }
                            }
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Step ${currentStep + 1} of ${steps.size}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            ElevatedCard(
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 220.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(24.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = steps[currentStep],
                                        style = MaterialTheme.typography.headlineSmall,
                                        lineHeight = 34.sp
                                    )
                                }
                            }

                            if (ingredients.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Quick Ingredient Check: ${ingredients.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { if (currentStep > 0) currentStep-- },
                                enabled = currentStep > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Text("Previous")
                            }
                            Button(
                                onClick = {
                                    if (currentStep < steps.lastIndex) currentStep++ else finish()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Text(if (currentStep < steps.lastIndex) "Next Step" else "Finish Cooking")
                            }
                        }
                    }
                }
            }
        }
    }
}
