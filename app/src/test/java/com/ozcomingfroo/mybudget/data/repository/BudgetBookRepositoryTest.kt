package com.ozcomingfroo.mybudget.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ozcomingfroo.mybudget.data.local.MyBudgetDatabase
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BudgetBookRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var database: MyBudgetDatabase
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: BudgetBookRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MyBudgetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { File(temporaryFolder.root, "app_preferences.preferences_pb") },
        )
        repository = BudgetBookRepository(
            database = database,
            budgetBookDao = database.budgetBookDao(),
            categoryDao = database.categoryDao(),
            appPreferencesRepository = AppPreferencesRepository(dataStore),
            clock = Clock.fixed(Instant.parse("2026-06-01T10:15:30Z"), ZoneOffset.UTC),
        )
    }

    @After
    fun tearDown() {
        database.close()
        dataStoreScope.cancel()
    }

    @Test
    fun renameBudgetBook_trimsAndUpdatesCurrentTitle() = runBlocking {
        val createdAt = Instant.parse("2026-01-01T00:00:00Z")
        val budgetBookId = database.budgetBookDao().insert(
            BudgetBookEntity(
                title = "Personal",
                createdAt = createdAt,
                updatedAt = createdAt,
            ),
        )

        repository.renameBudgetBook(budgetBookId, "  Home Budget  ")

        val updated = database.budgetBookDao().getById(budgetBookId)
        assertEquals("Home Budget", updated?.title)
        assertEquals(Instant.parse("2026-06-01T10:15:30Z"), updated?.updatedAt)
    }

    @Test
    fun localizeStarterCategories_updatesStarterTitlesWithoutDuplicatingOrTouchingCustomCategories() =
        runBlocking {
            val budgetBookId = repository.createBudgetBook("Personal")
            val createdAt = Instant.parse("2026-01-01T00:00:00Z")
            database.categoryDao().insert(
                CategoryEntity(
                    budgetBookId = budgetBookId,
                    title = "My custom groceries",
                    type = CategoryType.EXPENSE,
                    iconName = "shopping_cart",
                    color = "#2E7D32",
                    sortOrder = 0,
                    createdAt = createdAt,
                    updatedAt = createdAt,
                ),
            )

            repository.localizeStarterCategories(
                budgetBookId = budgetBookId,
                starterCategoryTitles = StarterCategories.englishTitles().map { title ->
                    when (title.key) {
                        StarterCategoryKey.GROCERIES -> title.copy(title = "Localized groceries")
                        StarterCategoryKey.SALARY -> title.copy(title = "Localized salary")
                        else -> title
                    }
                },
            )

            val categories = database.categoryDao().getForBudgetBook(budgetBookId)
            val titles = categories.map { it.title }

            assertEquals(17, categories.size)
            assertTrue("Localized groceries" in titles)
            assertTrue("Localized salary" in titles)
            assertTrue("My custom groceries" in titles)
            assertFalse("Groceries" in titles)
            assertFalse("Salary" in titles)
        }
}
