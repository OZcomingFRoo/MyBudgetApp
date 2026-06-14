package com.ozcomingfroo.mybudget.data.preferences

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
    val themeMode: AppThemeMode,
    val languageMode: AppLanguageMode,
    val hasCompletedOnboarding: Boolean,
    val defaultTransactionType: DefaultTransactionType,
)

@Singleton
class AppPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val preferences: Flow<AppPreferences> = dataStore.data.map { preferences ->
        AppPreferences(
            selectedBudgetBookId = preferences[SELECTED_BUDGET_BOOK_ID],
            themeMode = AppThemeMode.fromStorageValue(preferences[THEME_MODE]),
            languageMode = AppLanguageMode.fromStorageValue(preferences[LANGUAGE_MODE]),
            hasCompletedOnboarding = preferences[HAS_COMPLETED_ONBOARDING] ?: false,
            defaultTransactionType = DefaultTransactionType.fromStorageValue(
                preferences[DEFAULT_TRANSACTION_TYPE],
            ),
        )
    }

    suspend fun setSelectedBudgetBookId(id: Long) {
        dataStore.edit { preferences ->
            preferences[SELECTED_BUDGET_BOOK_ID] = id
        }
    }

    suspend fun getPreferences(): AppPreferences = preferences.first()

    suspend fun setThemeMode(themeMode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.name
        }
    }

    suspend fun setLanguageMode(languageMode: AppLanguageMode) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_MODE] = languageMode.name
        }
    }

    suspend fun setHasCompletedOnboarding(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun setDefaultTransactionType(defaultTransactionType: DefaultTransactionType) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_TRANSACTION_TYPE] = defaultTransactionType.name
        }
    }

    private companion object {
        val SELECTED_BUDGET_BOOK_ID = longPreferencesKey("selected_budget_book_id")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE_MODE = stringPreferencesKey("language_mode")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val DEFAULT_TRANSACTION_TYPE = stringPreferencesKey("default_transaction_type")
    }
}
