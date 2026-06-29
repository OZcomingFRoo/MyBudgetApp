package com.ozcomingfroo.mybudget.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ozcomingfroo.mybudget.widget.BalanceWidgetUpdateNotifier
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
    val dailyReminderEnabled: Boolean,
    val dailyReminderHour: Int,
    val dailyReminderMinute: Int,
)

@Singleton
class AppPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val widgetUpdateNotifier: BalanceWidgetUpdateNotifier? = null,
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
            dailyReminderEnabled = preferences[DAILY_REMINDER_ENABLED] ?: true,
            dailyReminderHour = preferences[DAILY_REMINDER_HOUR]?.coerceIn(0, 23) ?: 20,
            dailyReminderMinute = preferences[DAILY_REMINDER_MINUTE]?.coerceIn(0, 59) ?: 0,
        )
    }

    suspend fun setSelectedBudgetBookId(id: Long) {
        dataStore.edit { preferences ->
            preferences[SELECTED_BUDGET_BOOK_ID] = id
        }
        widgetUpdateNotifier?.notifyWidgetsChanged()
    }

    suspend fun getPreferences(): AppPreferences = preferences.first()

    suspend fun setThemeMode(themeMode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.name
        }
        widgetUpdateNotifier?.notifyWidgetsChanged()
    }

    suspend fun setLanguageMode(languageMode: AppLanguageMode) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_MODE] = languageMode.name
        }
        widgetUpdateNotifier?.notifyWidgetsChanged()
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

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DAILY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setDailyReminderTime(hour: Int, minute: Int) {
        dataStore.edit { preferences ->
            preferences[DAILY_REMINDER_HOUR] = hour.coerceIn(0, 23)
            preferences[DAILY_REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    private companion object {
        val SELECTED_BUDGET_BOOK_ID = longPreferencesKey("selected_budget_book_id")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE_MODE = stringPreferencesKey("language_mode")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val DEFAULT_TRANSACTION_TYPE = stringPreferencesKey("default_transaction_type")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
    }
}
