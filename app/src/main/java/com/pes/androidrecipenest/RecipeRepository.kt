package com.pes.androidrecipenest

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepository(
    private val api: MealApiService,
    private val dao: RecipeDao,
    private val searchHistoryDao: SearchHistoryDao
) {
    val favoriteRecipes: Flow<List<Meal>> =
        dao.getFavoriteRecipes().map { list -> list.map { it.toMeal() } }

    val personalRecipes: Flow<List<Meal>> =
        dao.getPersonalRecipes().map { list -> list.map { it.toMeal() } }

    val recentSearches: Flow<List<RecentSearchEntity>> = searchHistoryDao.getRecentSearches()

    fun isRecipeSaved(id: String): Flow<Boolean> = dao.isSaved(id)

    suspend fun searchMeals(query: String, byIngredient: Boolean): List<Meal> {
        val response = if (byIngredient && query.isNotBlank()) {
            api.filterByIngredient(query.trim())
        } else {
            api.searchByName(query.trim())
        }
        return response.meals?.map { it.toMeal() } ?: emptyList()
    }

    suspend fun filterByCategory(category: String): List<Meal> {
        val response = if (category == "All") {
            api.searchByName("")
        } else {
            api.filterByCategory(category)
        }
        return response.meals?.map { it.toMeal() } ?: emptyList()
    }

    suspend fun filterByCuisine(area: String): List<Meal> {
        return api.filterByArea(area).meals?.map { it.toMeal() } ?: emptyList()
    }

    suspend fun getCategories(): List<String> {
        val apiCategories = api.getCategories().categories?.map { it.name } ?: emptyList()
        return listOf("All") + apiCategories
    }

    suspend fun getRandomMeal(): Meal? {
        return api.getRandomMeal().meals?.firstOrNull()?.toMeal()
    }

    suspend fun getFullMealDetails(id: String): Meal? {
        dao.getRecipeById(id)?.let { return it.toMeal() }
        return api.getMealDetails(id).meals?.firstOrNull()?.toMeal()
    }

    suspend fun toggleFavorite(meal: Meal, currentlySaved: Boolean) {
        if (currentlySaved) {
            dao.deleteRecipe(meal.toEntity())
        } else {
            val fullMeal = if (meal.instructions.isBlank() && !meal.isCustom) {
                getFullMealDetails(meal.id) ?: meal
            } else {
                meal
            }
            dao.saveRecipe(fullMeal.copy(isCustom = false).toEntity())
        }
    }

    suspend fun savePersonalRecipe(meal: Meal) {
        dao.saveRecipe(meal.copy(isCustom = true).toEntity())
    }

    suspend fun deleteRecipe(meal: Meal) {
        dao.deleteRecipe(meal.toEntity())
    }

    suspend fun saveSearchQuery(query: String) {
        val clean = query.trim()
        if (clean.isNotBlank()) {
            searchHistoryDao.insertSearch(RecentSearchEntity(query = clean))
        }
    }

    suspend fun clearSearchHistory() {
        searchHistoryDao.clearAllSearches()
    }
}