package com.example.mybudget.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class AppPreferences(
    val selectedBudgetBookId: Long?,
    val themeMode: String,
    val languageTag: String,
    val hasCompletedOnboarding: Boolean,
)

@Singleton
class AppPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val preferences: Flow<AppPreferences> = dataStore.data.map { preferences ->
        AppPreferences(
            selectedBudgetBookId = preferences[SELECTED_BUDGET_BOOK_ID],
            themeMode = preferences[THEME_MODE] ?: DEFAULT_THEME_MODE,
            languageTag = preferences[LANGUAGE_TAG] ?: DEFAULT_LANGUAGE_TAG,
            hasCompletedOnboarding = preferences[HAS_COMPLETED_ONBOARDING] ?: false,
        )
    }

    suspend fun setSelectedBudgetBookId(id: Long) {
        dataStore.edit { preferences ->
            preferences[SELECTED_BUDGET_BOOK_ID] = id
        }
    }

    suspend fun getPreferences(): AppPreferences = preferences.first()

    suspend fun setThemeMode(themeMode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode
        }
    }

    suspend fun setLanguageTag(languageTag: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_TAG] = languageTag
        }
    }

    suspend fun setHasCompletedOnboarding(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    private companion object {
        val SELECTED_BUDGET_BOOK_ID = longPreferencesKey("selected_budget_book_id")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE_TAG = stringPreferencesKey("language_tag")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")

        const val DEFAULT_THEME_MODE = "default"
        const val DEFAULT_LANGUAGE_TAG = "en-US"
    }
}
