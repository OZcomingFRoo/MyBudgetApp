package com.ozcomingfroo.mybudget.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ozcomingfroo.mybudget.data.local.MyBudgetDatabase
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.local.entity.RecurringTransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import com.ozcomingfroo.mybudget.data.local.model.RecurringFrequency
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CategoryRepositoryTest {
    private lateinit var database: MyBudgetDatabase
    private lateinit var repository: CategoryRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MyBudgetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CategoryRepository(
            database = database,
            categoryDao = database.categoryDao(),
            recurringTransactionDao = database.recurringTransactionDao(),
            clock = Clock.fixed(Instant.parse("2026-06-01T10:15:30Z"), ZoneOffset.UTC),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createCategory_trimsTitleAndSetsTimestamps() = runBlocking {
        val budgetBookId = createBudgetBook()

        val categoryId = repository.createCategory(
            budgetBookId = budgetBookId,
            title = "  Travel  ",
            type = CategoryType.EXPENSE,
            iconName = "directions_car",
            color = "#2563EB",
            sortOrder = 4,
        )

        val category = database.categoryDao().getById(categoryId)
        assertEquals("Travel", category?.title)
        assertEquals(CategoryType.EXPENSE, category?.type)
        assertEquals("directions_car", category?.iconName)
        assertEquals("#2563EB", category?.color)
        assertEquals(4, category?.sortOrder)
        assertEquals(Instant.parse("2026-06-01T10:15:30Z"), category?.createdAt)
        assertEquals(Instant.parse("2026-06-01T10:15:30Z"), category?.updatedAt)
    }

    @Test
    fun archive_hidesCategoryFromActiveList() = runBlocking {
        val budgetBookId = createBudgetBook()
        val categoryId = repository.createCategory(
            budgetBookId = budgetBookId,
            title = "Travel",
            type = CategoryType.EXPENSE,
            iconName = "directions_car",
            color = "#2563EB",
            sortOrder = 0,
        )

        repository.archive(categoryId)

        val archived = database.categoryDao().getById(categoryId)
        val activeCategories = database.categoryDao().observeActiveForBudgetBook(budgetBookId).first()
        assertEquals(Instant.parse("2026-06-01T10:15:30Z"), archived?.archivedAt)
        assertEquals(Instant.parse("2026-06-01T10:15:30Z"), archived?.updatedAt)
        assertEquals(0, activeCategories.size)
    }

    @Test
    fun archive_deletesRecurringTransactionsForArchivedCategoryOnly() = runBlocking {
        val budgetBookId = createBudgetBook()
        val categoryId = repository.createCategory(
            budgetBookId = budgetBookId,
            title = "Rent",
            type = CategoryType.EXPENSE,
            iconName = "home",
            color = "#334155",
            sortOrder = 0,
        )
        val otherCategoryId = repository.createCategory(
            budgetBookId = budgetBookId,
            title = "Salary",
            type = CategoryType.INCOME,
            iconName = "payments",
            color = "#15803D",
            sortOrder = 1,
        )
        val removedRuleId = insertRecurringTransaction(
            budgetBookId = budgetBookId,
            categoryId = categoryId,
            title = "Monthly rent",
        )
        val retainedRuleId = insertRecurringTransaction(
            budgetBookId = budgetBookId,
            categoryId = otherCategoryId,
            title = "Monthly salary",
        )

        repository.archive(categoryId)

        val archived = database.categoryDao().getById(categoryId)
        val activeCategories = database.categoryDao().observeActiveForBudgetBook(budgetBookId).first()
        assertEquals(Instant.parse("2026-06-01T10:15:30Z"), archived?.archivedAt)
        assertEquals(listOf(otherCategoryId), activeCategories.map { it.id })
        assertEquals(null, database.recurringTransactionDao().getById(removedRuleId))
        assertEquals(retainedRuleId, database.recurringTransactionDao().getById(retainedRuleId)?.id)
    }

    private suspend fun createBudgetBook(): Long {
        val createdAt = Instant.parse("2026-01-01T00:00:00Z")
        return database.budgetBookDao().insert(
            BudgetBookEntity(
                title = "Personal",
                createdAt = createdAt,
                updatedAt = createdAt,
            ),
        )
    }

    private suspend fun insertRecurringTransaction(
        budgetBookId: Long,
        categoryId: Long,
        title: String,
    ): Long {
        val createdAt = Instant.parse("2026-01-01T00:00:00Z")
        return database.recurringTransactionDao().insert(
            RecurringTransactionEntity(
                budgetBookId = budgetBookId,
                categoryId = categoryId,
                type = TransactionType.EXPENSE,
                amountMinor = 120000,
                title = title,
                frequency = RecurringFrequency.MONTHLY,
                interval = 1,
                startDate = LocalDate.of(2026, 1, 1),
                nextRunDate = LocalDate.of(2026, 7, 1),
                createdAt = createdAt,
                updatedAt = createdAt,
            ),
        )
    }
}
