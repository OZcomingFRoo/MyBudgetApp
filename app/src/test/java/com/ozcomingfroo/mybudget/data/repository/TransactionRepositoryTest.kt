package com.ozcomingfroo.mybudget.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ozcomingfroo.mybudget.data.local.MyBudgetDatabase
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import com.ozcomingfroo.mybudget.widget.BalanceWidgetUpdateNotifier
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TransactionRepositoryTest {
    private lateinit var database: MyBudgetDatabase
    private lateinit var repository: TransactionRepository
    private val clock = Clock.fixed(Instant.parse("2026-06-01T10:15:30Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MyBudgetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TransactionRepository(
            database = database,
            transactionDao = database.transactionDao(),
            budgetBookDao = database.budgetBookDao(),
            clock = clock,
            widgetUpdateNotifier = BalanceWidgetUpdateNotifier(context),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndDelete_updateCachedTotals() = runBlocking {
        val budgetBookId = createBudgetBook("Personal")
        val incomeId = repository.insert(transaction(budgetBookId, TransactionType.INCOME, 150_000))
        val expenseId = repository.insert(transaction(budgetBookId, TransactionType.EXPENSE, 25_500))

        assertTotals(budgetBookId, income = 150_000, expense = 25_500)

        repository.delete(transaction(budgetBookId, TransactionType.EXPENSE, 25_500).copy(id = expenseId))
        repository.delete(transaction(budgetBookId, TransactionType.INCOME, 150_000).copy(id = incomeId))

        assertTotals(budgetBookId, income = 0, expense = 0)
    }

    @Test
    fun update_reversesOldTransactionAndAppliesNewTransaction() = runBlocking {
        val sourceBudgetBookId = createBudgetBook("Personal")
        val targetBudgetBookId = createBudgetBook("Work")
        val transactionId = repository.insert(
            transaction(sourceBudgetBookId, TransactionType.EXPENSE, 10_000),
        )

        repository.update(
            transaction(targetBudgetBookId, TransactionType.INCOME, 20_000).copy(id = transactionId),
        )

        assertTotals(sourceBudgetBookId, income = 0, expense = 0)
        assertTotals(targetBudgetBookId, income = 20_000, expense = 0)
    }

    private suspend fun createBudgetBook(title: String): Long {
        val now = clock.instant()
        return database.budgetBookDao().insert(
            BudgetBookEntity(
                title = title,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    private fun transaction(
        budgetBookId: Long,
        type: TransactionType,
        amountMinor: Long,
    ) = TransactionEntity(
        budgetBookId = budgetBookId,
        type = type,
        amountMinor = amountMinor,
        occurredAt = LocalDateTime.of(2026, 6, 1, 12, 0),
        createdAt = clock.instant(),
        updatedAt = clock.instant(),
    )

    private suspend fun assertTotals(budgetBookId: Long, income: Long, expense: Long) {
        val budgetBook = database.budgetBookDao().getById(budgetBookId)
        assertEquals(income, budgetBook?.totalIncomeMinor)
        assertEquals(expense, budgetBook?.totalExpenseMinor)
    }
}
