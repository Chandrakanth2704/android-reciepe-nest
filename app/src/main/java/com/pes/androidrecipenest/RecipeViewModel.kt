package com.pes.androidrecipenest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.contains
import kotlin.text.category

sealed interface RecipeUiState {
    data object Loading : RecipeUiState
    data class Success(val meals: List<Meal>) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = RecipeRepository(
        api = RetrofitInstance.api,
        dao = db.recipeDao(),
        searchHistoryDao = db.searchHistoryDao()
    )
    private val prefs = PreferencesManager(application)

    private val nonVegCategories = setOf("Beef", "Chicken", "Goat", "Lamb", "Pork", "Seafood")

    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(listOf("All"))
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedMeal = MutableStateFlow<Meal?>(null)
    val selectedMeal: StateFlow<Meal?> = _selectedMeal.asStateFlow()

    private val _dietFilter = MutableStateFlow(prefs.dietFilter)
    val dietFilter: StateFlow<DietFilter> = _dietFilter.asStateFlow()

    private val _userName = MutableStateFlow(prefs.userName)
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userBio = MutableStateFlow(prefs.userBio)
    val userBio: StateFlow<String> = _userBio.asStateFlow()

    private val _skillLevel = MutableStateFlow(prefs.skillLevel)
    val skillLevel: StateFlow<String> = _skillLevel.asStateFlow()

    private val _avatarUri = MutableStateFlow(prefs.avatarUri)
    val avatarUri: StateFlow<String> = _avatarUri.asStateFlow()

    val savedLastQuery: String
        get() = prefs.lastSearchQuery

    val favoriteRecipes: StateFlow<List<Meal>> = repository.favoriteRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalRecipes: StateFlow<List<Meal>> = repository.personalRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSearches: StateFlow<List<RecentSearchEntity>> = repository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val allCats = repository.getCategories()
                _categories.value = when (_dietFilter.value) {
                    DietFilter.VEG -> allCats.filterNot { it in nonVegCategories }
                    DietFilter.NON_VEG -> listOf("All") + allCats.filter { it in nonVegCategories }
                }
            } catch (_: Exception) { }

            val initialQuery = prefs.lastSearchQuery
            if (initialQuery.isNotBlank()) {
                searchRecipes(initialQuery, byIngredient = false)
            } else {
                selectCategory("All")
            }
        }
    }

    fun toggleDietFilter() {
        val nextFilter = if (_dietFilter.value == DietFilter.VEG) DietFilter.NON_VEG else DietFilter.VEG
        prefs.dietFilter = nextFilter
        _dietFilter.value = nextFilter
        loadInitialData()
    }

    fun updateUserProfile(name: String, bio: String) {
        val cleanName = name.trim().ifBlank { "Home Chef" }
        val cleanBio = bio.trim()
        prefs.userName = cleanName
        prefs.userBio = cleanBio
        _userName.value = cleanName
        _userBio.value = cleanBio
    }

    fun updateSkillLevel(level: String) {
        prefs.skillLevel = level
        _skillLevel.value = level
    }

    fun updateAvatarUri(uri: String) {
        prefs.avatarUri = uri
        _avatarUri.value = uri
    }

    fun searchRecipes(query: String, byIngredient: Boolean) {
        prefs.lastSearchQuery = query
        viewModelScope.launch {
            if (query.isNotBlank()) {
                repository.saveSearchQuery(query)
            }
            _uiState.value = RecipeUiState.Loading
            try {
                val rawResults = repository.searchMeals(query, byIngredient)
                val filtered = applyDietFilter(rawResults, isCategoryFiltered = false)
                _uiState.value = RecipeUiState.Success(filtered)
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error("Unable to load recipes. Check your internet connection.")
            }
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    fun selectCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = RecipeUiState.Loading
            try {
                val target = when {
                    _dietFilter.value == DietFilter.VEG && category == "All" -> "Vegetarian"
                    _dietFilter.value == DietFilter.NON_VEG && category == "All" -> "Chicken"
                    else -> category
                }
                val rawResults = repository.filterByCategory(target)
                val filtered = applyDietFilter(rawResults, isCategoryFiltered = true)
                _uiState.value = RecipeUiState.Success(filtered)
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error("Failed to filter by $category.")
            }
        }
    }

    fun selectCuisine(cuisine: String) {
        viewModelScope.launch {
            _uiState.value = RecipeUiState.Loading
            try {
                val rawResults = repository.filterByCuisine(cuisine)
                _uiState.value = RecipeUiState.Success(rawResults)
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error("Failed to load $cuisine dishes.")
            }
        }
    }

    private fun applyDietFilter(meals: List<Meal>, isCategoryFiltered: Boolean): List<Meal> {
        return when (_dietFilter.value) {
            DietFilter.VEG -> meals.filterNot { it.category in nonVegCategories }
            DietFilter.NON_VEG -> if (isCategoryFiltered) {
                meals
            } else {
                meals.filter { it.category in nonVegCategories || it.category == "General" }
            }
        }
    }

    fun fetchRandomRecipe(onMealFound: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.getRandomMeal()?.let { meal ->
                    onMealFound(meal.id)
                }
            } catch (_: Exception) { }
        }
    }

    fun loadMealDetail(mealId: String) {
        viewModelScope.launch {
            _selectedMeal.value = null
            try {
                _selectedMeal.value = repository.getFullMealDetails(mealId)
            } catch (_: Exception) {
                _selectedMeal.value = null
            }
        }
    }

    suspend fun getMealForEdit(mealId: String): Meal? {
        return repository.getFullMealDetails(mealId)
    }

    fun isMealSaved(id: String): Flow<Boolean> = repository.isRecipeSaved(id)

    fun toggleFavorite(meal: Meal, isCurrentlySaved: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(meal, isCurrentlySaved)
        }
    }

    fun saveOrUpdatePersonalRecipe(
        existingId: String? = null,
        name: String,
        category: String,
        area: String,
        instructions: String,
        imageUri: String,
        ingredients: List<Pair<String, String>>,
        prepTimeMinutes: Int = 30,
        difficulty: String = "Medium",
        servings: Int = 2,
        isVeg: Boolean = true
    ) {
        viewModelScope.launch {
            val meal = Meal(
                id = existingId ?: "custom_${UUID.randomUUID()}",
                name = name.trim(),
                category = category.ifBlank { "Homemade" },
                area = area.ifBlank { "Personal" },
                instructions = instructions.trim(),
                imageUri = imageUri,
                ingredients = ingredients.filter { it.first.isNotBlank() },
                isCustom = true,
                prepTimeMinutes = prepTimeMinutes,
                difficulty = difficulty,
                servings = servings,
                isVeg = isVeg
            )
            repository.savePersonalRecipe(meal)
        }
    }

    fun deleteRecipe(meal: Meal) {
        viewModelScope.launch {
            repository.deleteRecipe(meal)
        }
    }
}