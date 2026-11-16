package com.pes.androidrecipenest

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {

    @GET("search.php")
    suspend fun searchByName(@Query("s") query: String): MealResponse

    @GET("filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): MealResponse

    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealResponse

    @GET("filter.php")
    suspend fun filterByArea(@Query("a") area: String): MealResponse

    @GET("lookup.php")
    suspend fun getMealDetails(@Query("i") id: String): MealResponse

    @GET("categories.php")
    suspend fun getCategories(): CategoryResponse

    @GET("random.php")
    suspend fun getRandomMeal(): MealResponse
}

object RetrofitInstance {
    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    val api: MealApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealApiService::class.java)
    }
}