package com.pes.androidrecipenest

import com.google.gson.annotations.SerializedName

data class MealResponse(
    @SerializedName("meals") val meals: List<MealDto>?
)

data class MealDto(
    @SerializedName("idMeal") val idMeal: String,
    @SerializedName("strMeal") val strMeal: String,
    @SerializedName("strCategory") val strCategory: String? = null,
    @SerializedName("strArea") val strArea: String? = null,
    @SerializedName("strInstructions") val strInstructions: String? = null,
    @SerializedName("strMealThumb") val strMealThumb: String? = null,
    @SerializedName("strYoutube") val strYoutube: String? = null,
    val strIngredient1: String? = null, val strIngredient2: String? = null,
    val strIngredient3: String? = null, val strIngredient4: String? = null,
    val strIngredient5: String? = null, val strIngredient6: String? = null,
    val strIngredient7: String? = null, val strIngredient8: String? = null,
    val strIngredient9: String? = null, val strIngredient10: String? = null,
    val strIngredient11: String? = null, val strIngredient12: String? = null,
    val strIngredient13: String? = null, val strIngredient14: String? = null,
    val strIngredient15: String? = null, val strIngredient16: String? = null,
    val strIngredient17: String? = null, val strIngredient18: String? = null,
    val strIngredient19: String? = null, val strIngredient20: String? = null,
    val strMeasure1: String? = null, val strMeasure2: String? = null,
    val strMeasure3: String? = null, val strMeasure4: String? = null,
    val strMeasure5: String? = null, val strMeasure6: String? = null,
    val strMeasure7: String? = null, val strMeasure8: String? = null,
    val strMeasure9: String? = null, val strMeasure10: String? = null,
    val strMeasure11: String? = null, val strMeasure12: String? = null,
    val strMeasure13: String? = null, val strMeasure14: String? = null,
    val strMeasure15: String? = null, val strMeasure16: String? = null,
    val strMeasure17: String? = null, val strMeasure18: String? = null,
    val strMeasure19: String? = null, val strMeasure20: String? = null
)

data class CategoryResponse(
    @SerializedName("categories") val categories: List<CategoryDto>?
)

data class CategoryDto(
    @SerializedName("idCategory") val id: String,
    @SerializedName("strCategory") val name: String,
    @SerializedName("strCategoryThumb") val thumbnailUrl: String
)

data class Meal(
    val id: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val imageUri: String,
    val youtubeUrl: String = "",
    val ingredients: List<Pair<String, String>>,
    val isCustom: Boolean = false,
    val prepTimeMinutes: Int = 30,
    val difficulty: String = "Medium",
    val servings: Int = 2,
    val isVeg: Boolean = true
)

fun MealDto.toMeal(): Meal {
    val rawPairs = listOf(
        strIngredient1 to strMeasure1, strIngredient2 to strMeasure2,
        strIngredient3 to strMeasure3, strIngredient4 to strMeasure4,
        strIngredient5 to strMeasure5, strIngredient6 to strMeasure6,
        strIngredient7 to strMeasure7, strIngredient8 to strMeasure8,
        strIngredient9 to strMeasure9, strIngredient10 to strMeasure10,
        strIngredient11 to strMeasure11, strIngredient12 to strMeasure12,
        strIngredient13 to strMeasure13, strIngredient14 to strMeasure14,
        strIngredient15 to strMeasure15, strIngredient16 to strMeasure16,
        strIngredient17 to strMeasure17, strIngredient18 to strMeasure18,
        strIngredient19 to strMeasure19, strIngredient20 to strMeasure20
    )

    val cleanIngredients = rawPairs.mapNotNull { (ing, meas) ->
        if (!ing.isNullOrBlank()) {
            ing.trim() to (meas?.trim().orEmpty())
        } else null
    }

    val nonVegCategories = setOf("Beef", "Chicken", "Goat", "Lamb", "Pork", "Seafood")
    val resolvedCategory = strCategory ?: "General"

    return Meal(
        id = idMeal,
        name = strMeal,
        category = resolvedCategory,
        area = strArea ?: "Global",
        instructions = strInstructions.orEmpty(),
        imageUri = strMealThumb.orEmpty(),
        youtubeUrl = strYoutube.orEmpty(),
        ingredients = cleanIngredients,
        isCustom = false,
        prepTimeMinutes = if (cleanIngredients.size > 10) 45 else 25,
        difficulty = if (cleanIngredients.size > 12) "Chef Special" else if (cleanIngredients.size > 7) "Medium" else "Easy",
        servings = 2,
        isVeg = resolvedCategory !in nonVegCategories
    )
}