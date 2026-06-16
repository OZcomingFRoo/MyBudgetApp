package com.ozcomingfroo.mybudget.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TransactionDaoTest {
    private lateinit var database: MyBudgetDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MyBudgetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun aggregateQueriesReturnMinorUnitTotalsFromSql() = runBlocking {
        val now = Instant.parse("2026-04-01T00:00:00Z")
        val budgetBookId = database.budgetBookDao().insert(
            BudgetBookEntity(title = "Personal", createdAt = now, updatedAt = now),
        )
        database.transactionDao().insertAll(
            listOf(
                transaction(budgetBookId, TransactionType.INCOME, 150000, LocalDate.of(2026, 4, 1), now),
                transaction(budgetBookId, TransactionType.EXPENSE, 2550, LocalDate.of(2026, 4, 2), now),
                transaction(
                    budgetBookId = budgetBookId,
                    type = TransactionType.EXPENSE,
                    amountMinor = 1050,
                    occurredAt = LocalDateTime.of(LocalDate.of(2026, 4, 30), LocalTime.of(23, 59)),
                    now = now,
                ),
                transaction(budgetBookId, TransactionType.EXPENSE, 9999, LocalDate.of(2026, 5, 1), now),
            ),
        )

        val expenseTotal = database.transactionDao().totalByType(
            budgetBookId = budgetBookId,
            type = TransactionType.EXPENSE,
            startDateTime = LocalDate.of(2026, 4, 1).atStartOfDay(),
            endExclusiveDateTime = LocalDate.of(2026, 5, 1).atStartOfDay(),
        )
        val balance = database.transactionDao().currentBalance(budgetBookId)
        val monthlyTotals = database.transactionDao().monthlyTotals(
            budgetBookId = budgetBookId,
            type = TransactionType.EXPENSE,
            startDateTime = LocalDate.of(2026, 4, 1).atStartOfDay(),
            endExclusiveDateTime = LocalDate.of(2026, 5, 1).atStartOfDay(),
        )

        assertEquals(3600L, expenseTotal)
        assertEquals(136401L, balance)
        assertEquals(listOf("2026-04"), monthlyTotals.map { it.month })
        assertEquals(listOf(3600L), monthlyTotals.map { it.totalMinor })
    }

    private fun transaction(
        budgetBookId: Long,
        type: TransactionType,
        amountMinor: Long,
        occurredDate: LocalDate,
        now: Instant,
    ) = TransactionEntity(
        budgetBookId = budgetBookId,
        type = type,
        amountMinor = amountMinor,
        occurredAt = occurredDate.atStartOfDay(),
        createdAt = now,
        updatedAt = now,
    )

    private fun transaction(
        budgetBookId: Long,
        type: TransactionType,
        amountMinor: Long,
        occurredAt: LocalDateTime,
        now: Instant,
    ) = TransactionEntity(
        budgetBookId = budgetBookId,
        type = type,
        amountMinor = amountMinor,
        occurredAt = occurredAt,
        createdAt = now,
        updatedAt = now,
    )
}
