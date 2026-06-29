package com.ozcomingfroo.mybudget.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AppPreferencesRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var scope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: AppPreferencesRepository

    @Before
    fun setUp() {
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { File(temporaryFolder.root, "app_preferences.preferences_pb") },
        )
        repository = AppPreferencesRepository(dataStore)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun getPreferences_returnsDefaultsWhenNothingIsStored() = runBlocking {
        val preferences = repository.getPreferences()

        assertNull(preferences.selectedBudgetBookId)
        assertEquals(AppThemeMode.DEFAULT, preferences.themeMode)
        assertEquals(AppLanguageMode.HE, preferences.languageMode)
        assertFalse(preferences.hasCompletedOnboarding)
        assertEquals(DefaultTransactionType.EXPENSE, preferences.defaultTransactionType)
        assertTrue(preferences.dailyReminderEnabled)
        assertEquals(20, preferences.dailyReminderHour)
        assertEquals(0, preferences.dailyReminderMinute)
    }

    @Test
    fun settersPersistTypedPreferenceValues() = runBlocking {
        repository.setSelectedBudgetBookId(42)
        repository.setThemeMode(AppThemeMode.NIGHT)
        repository.setLanguageMode(AppLanguageMode.HE)
        repository.setHasCompletedOnboarding(true)
        repository.setDefaultTransactionType(DefaultTransactionType.INCOME)
        repository.setDailyReminderEnabled(false)
        repository.setDailyReminderTime(8, 30)

        val preferences = repository.getPreferences()

        assertEquals(42L, preferences.selectedBudgetBookId)
        assertEquals(AppThemeMode.NIGHT, preferences.themeMode)
        assertEquals(AppLanguageMode.HE, preferences.languageMode)
        assertEquals(true, preferences.hasCompletedOnboarding)
        assertEquals(DefaultTransactionType.INCOME, preferences.defaultTransactionType)
        assertFalse(preferences.dailyReminderEnabled)
        assertEquals(8, preferences.dailyReminderHour)
        assertEquals(30, preferences.dailyReminderMinute)
    }

    @Test
    fun getPreferences_fallsBackToDefaultsForUnknownEnumValues() = runBlocking {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("theme_mode")] = "PURPLE"
            preferences[stringPreferencesKey("language_mode")] = "KLINGON"
            preferences[stringPreferencesKey("default_transaction_type")] = "TRANSFER"
        }

        val preferences = repository.getPreferences()

        assertEquals(AppThemeMode.DEFAULT, preferences.themeMode)
        assertEquals(AppLanguageMode.HE, preferences.languageMode)
        assertEquals(DefaultTransactionType.EXPENSE, preferences.defaultTransactionType)
    }
}
