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
import com.ozcomingfroo.mybudget.data.local.entity.RecurringTransactionEntity
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import com.ozcomingfroo.mybudget.data.local.model.RecurringFrequency
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BudgetBookRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var database: MyBudgetDatabase
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var appPreferencesRepository: AppPreferencesRepository
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
        appPreferencesRepository = AppPreferencesRepository(dataStore)
        repository = BudgetBookRepository(
            database = database,
            budgetBookDao = database.budgetBookDao(),
            categoryDao = database.categoryDao(),
            appPreferencesRepository = appPreferencesRepository,
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
    fun updateBudgetBookDetails_trimsTitleAndStoresDescription() = runBlocking {
        val createdAt = Instant.parse("2026-01-01T00:00:00Z")
        val budgetBookId = database.budgetBookDao().insert(
            BudgetBookEntity(
                title = "Personal",
                description = "Old",
                createdAt = createdAt,
                updatedAt = createdAt,
            ),
        )

        repository.updateBudgetBookDetails(
            id = budgetBookId,
            title = "  Family  ",
            description = "  Household spending  ",
        )

        val updated = database.budgetBookDao().getById(budgetBookId)
        assertEquals("Family", updated?.title)
        assertEquals("Household spending", updated?.description)
        assertEquals(Instant.parse("2026-06-01T10:15:30Z"), updated?.updatedAt)
    }

    @Test
    fun updateBudgetBookDetails_convertsBlankDescriptionToNull() = runBlocking {
        val budgetBookId = repository.createBudgetBook(
            title = "Personal",
            description = "Temporary",
        )

        repository.updateBudgetBookDetails(
            id = budgetBookId,
            title = "Personal",
            description = "   ",
        )

        val updated = database.budgetBookDao().getById(budgetBookId)
        assertNull(updated?.description)
    }

    @Test
    fun createBudgetBook_storesDescriptionCreatesStarterCategoriesAndSelectsIt() = runBlocking {
        val budgetBookId = repository.createBudgetBook(
            title = "  Freelance  ",
            description = "  Client work  ",
        )

        val budgetBook = database.budgetBookDao().getById(budgetBookId)
        val categories = database.categoryDao().getForBudgetBook(budgetBookId)
        val preferences = appPreferencesRepository.getPreferences()

        assertEquals("Freelance", budgetBook?.title)
        assertEquals("Client work", budgetBook?.description)
        assertTrue(categories.isNotEmpty())
        assertEquals(budgetBookId, preferences.selectedBudgetBookId)
    }

    @Test
    fun archiveBudgetBook_archivesNonCurrentBudgetBookAndKeepsAssociatedData() = runBlocking {
        val currentBudgetBookId = repository.createBudgetBook("Personal")
        val archivedBudgetBookId = repository.createBudgetBook("Work", selectAfterCreate = false)
        appPreferencesRepository.setSelectedBudgetBookId(currentBudgetBookId)
        val categoryCountBefore = database.categoryDao().getForBudgetBook(archivedBudgetBookId).size

        val archived = repository.archiveBudgetBook(archivedBudgetBookId)

        val activeBudgetBookIds = repository.observeActiveBudgetBooks().first().map { it.id }
        val archivedBudgetBook = database.budgetBookDao().getById(archivedBudgetBookId)
        val categoryCountAfter = database.categoryDao().getForBudgetBook(archivedBudgetBookId).size

        assertTrue(archived)
        assertFalse(archivedBudgetBookId in activeBudgetBookIds)
        assertEquals(Instant.parse("2026-06-01T10:15:30Z"), archivedBudgetBook?.archivedAt)
        assertEquals(categoryCountBefore, categoryCountAfter)
    }

    @Test
    fun observeArchivedBudgetBooks_returnsArchivedBudgetBooksOnly() = runBlocking {
        val currentBudgetBookId = repository.createBudgetBook("Personal")
        val archivedBudgetBookId = repository.createBudgetBook("Work", selectAfterCreate = false)
        appPreferencesRepository.setSelectedBudgetBookId(currentBudgetBookId)

        repository.archiveBudgetBook(archivedBudgetBookId)

        val archivedBudgetBookIds = repository.observeArchivedBudgetBooks().first().map { it.id }
        val activeBudgetBookIds = repository.observeActiveBudgetBooks().first().map { it.id }

        assertTrue(archivedBudgetBookId in archivedBudgetBookIds)
        assertFalse(currentBudgetBookId in archivedBudgetBookIds)
        assertFalse(archivedBudgetBookId in activeBudgetBookIds)
    }

    @Test
    fun restoreBudgetBook_clearsArchivedAtAndReturnsItToActiveBudgetBooks() = runBlocking {
        val currentBudgetBookId = repository.createBudgetBook("Personal")
        val archivedBudgetBookId = repository.createBudgetBook("Work", selectAfterCreate = false)
        appPreferencesRepository.setSelectedBudgetBookId(currentBudgetBookId)
        repository.archiveBudgetBook(archivedBudgetBookId)

        val restored = repository.restoreBudgetBook(archivedBudgetBookId)

        val restoredBudgetBook = database.budgetBookDao().getById(archivedBudgetBookId)
        val activeBudgetBookIds = repository.observeActiveBudgetBooks().first().map { it.id }
        val archivedBudgetBookIds = repository.observeArchivedBudgetBooks().first().map { it.id }

        assertTrue(restored)
        assertNull(restoredBudgetBook?.archivedAt)
        assertEquals(Instant.parse("2026-06-01T10:15:30Z"), restoredBudgetBook?.updatedAt)
        assertTrue(archivedBudgetBookId in activeBudgetBookIds)
        assertFalse(archivedBudgetBookId in archivedBudgetBookIds)
    }

    @Test
    fun restoreBudgetBook_returnsFalseForActiveOrMissingBudgetBook() = runBlocking {
        val activeBudgetBookId = repository.createBudgetBook("Personal")

        assertFalse(repository.restoreBudgetBook(activeBudgetBookId))
        assertFalse(repository.restoreBudgetBook(9999L))
    }

    @Test
    fun getWidgetCandidate_prefersSelectedActiveThenFirstActiveThenFirstBook() = runBlocking {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val archivedBookId = database.budgetBookDao().insert(
            BudgetBookEntity(
                title = "Archived",
                archivedAt = now,
                createdAt = now,
                updatedAt = now,
            ),
        )
        val zBookId = database.budgetBookDao().insert(
            BudgetBookEntity(title = "Z Personal", createdAt = now, updatedAt = now),
        )
        val aBookId = database.budgetBookDao().insert(
            BudgetBookEntity(title = "A Household", createdAt = now, updatedAt = now),
        )

        assertEquals(zBookId, database.budgetBookDao().getWidgetCandidate(zBookId)?.id)
        assertEquals(aBookId, database.budgetBookDao().getWidgetCandidate(archivedBookId)?.id)

        database.budgetBookDao().archive(zBookId, archivedAt = now, updatedAt = now)
        database.budgetBookDao().archive(aBookId, archivedAt = now, updatedAt = now)

        assertEquals(archivedBookId, database.budgetBookDao().getWidgetCandidate(zBookId)?.id)
    }

    @Test
    fun deleteBudgetBookPermanently_deletesNonCurrentBudgetBookAndAssociatedData() = runBlocking {
        val currentBudgetBookId = repository.createBudgetBook("Personal")
        val deletedBudgetBookId = repository.createBudgetBook("Work", selectAfterCreate = false)
        appPreferencesRepository.setSelectedBudgetBookId(currentBudgetBookId)
        val categoryId = database.categoryDao().getForBudgetBook(deletedBudgetBookId).first().id
        val now = Instant.parse("2026-01-01T00:00:00Z")
        database.transactionDao().insert(
            TransactionEntity(
                budgetBookId = deletedBudgetBookId,
                categoryId = categoryId,
                type = TransactionType.EXPENSE,
                amountMinor = 2500,
                occurredAt = LocalDateTime.of(2026, 6, 1, 12, 0),
                createdAt = now,
                updatedAt = now,
            ),
        )
        database.recurringTransactionDao().insert(
            RecurringTransactionEntity(
                budgetBookId = deletedBudgetBookId,
                categoryId = categoryId,
                type = TransactionType.EXPENSE,
                amountMinor = 2500,
                frequency = RecurringFrequency.MONTHLY,
                interval = 1,
                startDate = LocalDate.of(2026, 6, 1),
                nextRunDate = LocalDate.of(2026, 7, 1),
                createdAt = now,
                updatedAt = now,
            ),
        )

        val deleted = repository.deleteBudgetBookPermanently(deletedBudgetBookId)

        assertTrue(deleted)
        assertNull(database.budgetBookDao().getById(deletedBudgetBookId))
        assertTrue(database.categoryDao().getForBudgetBook(deletedBudgetBookId).isEmpty())
        assertTrue(database.transactionDao().getForBudgetBook(deletedBudgetBookId).isEmpty())
        assertTrue(database.recurringTransactionDao().observeForBudgetBook(deletedBudgetBookId).first().isEmpty())
    }

    @Test
    fun deleteBudgetBookPermanently_deletesArchivedBudgetBookAndAssociatedData() = runBlocking {
        val currentBudgetBookId = repository.createBudgetBook("Personal")
        val deletedBudgetBookId = repository.createBudgetBook("Work", selectAfterCreate = false)
        appPreferencesRepository.setSelectedBudgetBookId(currentBudgetBookId)
        val categoryId = database.categoryDao().getForBudgetBook(deletedBudgetBookId).first().id
        val now = Instant.parse("2026-01-01T00:00:00Z")
        database.transactionDao().insert(
            TransactionEntity(
                budgetBookId = deletedBudgetBookId,
                categoryId = categoryId,
                type = TransactionType.EXPENSE,
                amountMinor = 2500,
                occurredAt = LocalDateTime.of(2026, 6, 1, 12, 0),
                createdAt = now,
                updatedAt = now,
            ),
        )
        repository.archiveBudgetBook(deletedBudgetBookId)

        val deleted = repository.deleteBudgetBookPermanently(deletedBudgetBookId)

        assertTrue(deleted)
        assertNull(database.budgetBookDao().getById(deletedBudgetBookId))
        assertTrue(database.categoryDao().getForBudgetBook(deletedBudgetBookId).isEmpty())
        assertTrue(database.transactionDao().getForBudgetBook(deletedBudgetBookId).isEmpty())
    }

    @Test
    fun archiveAndDeleteBudgetBook_failForSelectedBudgetBook() = runBlocking {
        val selectedBudgetBookId = repository.createBudgetBook("Personal")
        repository.createBudgetBook("Work", selectAfterCreate = false)
        appPreferencesRepository.setSelectedBudgetBookId(selectedBudgetBookId)

        val archived = repository.archiveBudgetBook(selectedBudgetBookId)
        val deleted = repository.deleteBudgetBookPermanently(selectedBudgetBookId)

        assertFalse(archived)
        assertFalse(deleted)
        assertTrue(selectedBudgetBookId in repository.observeActiveBudgetBooks().first().map { it.id })
    }

    @Test
    fun archiveAndDeleteBudgetBook_failWhenOnlyOneActiveBudgetBookExists() = runBlocking {
        val onlyBudgetBookId = repository.createBudgetBook("Personal", selectAfterCreate = false)

        val archived = repository.archiveBudgetBook(onlyBudgetBookId)
        val deleted = repository.deleteBudgetBookPermanently(onlyBudgetBookId)

        assertFalse(archived)
        assertFalse(deleted)
        assertTrue(onlyBudgetBookId in repository.observeActiveBudgetBooks().first().map { it.id })
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
