package com.pes.androidrecipenest

import android.content.Context

enum class DietFilter {
    VEG, NON_VEG
}

class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("recipe_cookbook_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DIET_FILTER = "key_diet_filter"
        private const val KEY_LAST_QUERY = "key_last_query"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_BIO = "key_user_bio"
        private const val KEY_SKILL_LEVEL = "key_skill_level"
        private const val KEY_AVATAR_URI = "key_avatar_uri"
    }

    var dietFilter: DietFilter
        get() {
            val saved = prefs.getString(KEY_DIET_FILTER, DietFilter.VEG.name) ?: DietFilter.VEG.name
            return runCatching { DietFilter.valueOf(saved) }.getOrDefault(DietFilter.VEG)
        }
        set(value) = prefs.edit().putString(KEY_DIET_FILTER, value.name).apply()

    var lastSearchQuery: String
        get() = prefs.getString(KEY_LAST_QUERY, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_LAST_QUERY, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Home Chef").orEmpty()
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userBio: String
        get() = prefs.getString(KEY_USER_BIO, "Passionate about homemade recipes & global flavors").orEmpty()
        set(value) = prefs.edit().putString(KEY_USER_BIO, value).apply()

    var skillLevel: String
        get() = prefs.getString(KEY_SKILL_LEVEL, "Home Cook").orEmpty()
        set(value) = prefs.edit().putString(KEY_SKILL_LEVEL, value).apply()

    var avatarUri: String
        get() = prefs.getString(KEY_AVATAR_URI, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_AVATAR_URI, value).apply()
}