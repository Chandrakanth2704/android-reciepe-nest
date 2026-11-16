package com.pes.androidrecipenest

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_recipes")
data class SavedRecipeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val imageUri: String,
    val youtubeUrl: String = "",
    val ingredients: List<Pair<String, String>>,
    val isCustom: Boolean,
    val prepTimeMinutes: Int = 30,
    val difficulty: String = "Medium",
    val servings: Int = 2,
    val isVeg: Boolean = true
)

fun SavedRecipeEntity.toMeal(): Meal = Meal(
    id = id,
    name = name,
    category = category,
    area = area,
    instructions = instructions,
    imageUri = imageUri,
    youtubeUrl = youtubeUrl,
    ingredients = ingredients,
    isCustom = isCustom,
    prepTimeMinutes = prepTimeMinutes,
    difficulty = difficulty,
    servings = servings,
    isVeg = isVeg
)

fun Meal.toEntity(): SavedRecipeEntity = SavedRecipeEntity(
    id = id,
    name = name,
    category = category,
    area = area,
    instructions = instructions,
    imageUri = imageUri,
    youtubeUrl = youtubeUrl,
    ingredients = ingredients,
    isCustom = isCustom,
    prepTimeMinutes = prepTimeMinutes,
    difficulty = difficulty,
    servings = servings,
    isVeg = isVeg
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

class IngredientTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromIngredientList(list: List<Pair<String, String>>): String = gson.toJson(list)

    @TypeConverter
    fun toIngredientList(json: String): List<Pair<String, String>> {
        val type = object : TypeToken<List<Pair<String, String>>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}

@Dao
interface RecipeDao {
    @Upsert
    suspend fun saveRecipe(recipe: SavedRecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: SavedRecipeEntity)

    @Query("SELECT * FROM saved_recipes WHERE isCustom = 0 ORDER BY name ASC")
    fun getFavoriteRecipes(): Flow<List<SavedRecipeEntity>>

    @Query("SELECT * FROM saved_recipes WHERE isCustom = 1 ORDER BY name ASC")
    fun getPersonalRecipes(): Flow<List<SavedRecipeEntity>>

    @Query("SELECT * FROM saved_recipes WHERE id = :id LIMIT 1")
    suspend fun getRecipeById(id: String): SavedRecipeEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM saved_recipes WHERE id = :id)")
    fun isSaved(id: String): Flow<Boolean>
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 6")
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>

    @Upsert
    suspend fun insertSearch(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches")
    suspend fun clearAllSearches()
}

@Database(
    entities = [SavedRecipeEntity::class, RecentSearchEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(IngredientTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipe_cookbook_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}