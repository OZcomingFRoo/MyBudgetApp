package com.ozcomingfroo.mybudget.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ozcomingfroo.mybudget.data.local.MyBudgetDatabase
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.local.entity.RecurringTransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.RecurringFrequency
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecurringTransactionRepositoryTest {
    private lateinit var database: MyBudgetDatabase
    private lateinit var repository: RecurringTransactionRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MyBudgetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RecurringTransactionRepository(
            recurringTransactionDao = database.recurringTransactionDao(),
            clock = Clock.fixed(Instant.parse("2026-06-01T10:15:30Z"), ZoneOffset.UTC),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun delete_removesRecurringTransaction() = runBlocking {
        val budgetBookId = createBudgetBook()
        val rule = recurringTransaction(budgetBookId = budgetBookId)
        val ruleId = repository.insert(rule)

        repository.delete(rule.copy(id = ruleId))

        assertNull(database.recurringTransactionDao().getById(ruleId))
    }

    @Test
    fun update_refreshesUpdatedAt() = runBlocking {
        val budgetBookId = createBudgetBook()
        val rule = recurringTransaction(budgetBookId = budgetBookId)
        val ruleId = repository.insert(rule)

        repository.update(rule.copy(id = ruleId, title = "Updated"))

        val updated = database.recurringTransactionDao().getById(ruleId)
        assertEquals("Updated", updated?.title)
        assertEquals(Instant.parse("2026-06-01T10:15:30Z"), updated?.updatedAt)
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

    private fun recurringTransaction(budgetBookId: Long) = RecurringTransactionEntity(
        budgetBookId = budgetBookId,
        type = TransactionType.EXPENSE,
        amountMinor = 12_000,
        title = "Rent",
        frequency = RecurringFrequency.MONTHLY,
        interval = 1,
        startDate = LocalDate.of(2026, 6, 1),
        nextRunDate = LocalDate.of(2026, 7, 1),
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
    )
}
