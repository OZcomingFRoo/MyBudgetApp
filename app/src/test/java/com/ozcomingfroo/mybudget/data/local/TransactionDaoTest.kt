package com.ozcomingfroo.mybudget.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.core.app.ApplicationProvider
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun observeForHistoryFilter_appliesDateTypeAndCategoryInSql() = runBlocking {
        val now = Instant.parse("2026-04-01T00:00:00Z")
        val budgetBookId = database.budgetBookDao().insert(
            BudgetBookEntity(title = "Personal", createdAt = now, updatedAt = now),
        )
        val groceriesId = database.categoryDao().insert(category(budgetBookId, "Groceries", now))
        val rentId = database.categoryDao().insert(category(budgetBookId, "Rent", now))
        database.transactionDao().insertAll(
            listOf(
                transaction(
                    budgetBookId = budgetBookId,
                    type = TransactionType.EXPENSE,
                    amountMinor = 2000,
                    occurredDate = LocalDate.of(2026, 4, 3),
                    now = now,
                ).copy(id = 1, categoryId = groceriesId),
                transaction(
                    budgetBookId = budgetBookId,
                    type = TransactionType.EXPENSE,
                    amountMinor = 9000,
                    occurredDate = LocalDate.of(2026, 4, 4),
                    now = now,
                ).copy(id = 2, categoryId = rentId),
                transaction(
                    budgetBookId = budgetBookId,
                    type = TransactionType.INCOME,
                    amountMinor = 100_000,
                    occurredDate = LocalDate.of(2026, 4, 5),
                    now = now,
                ).copy(id = 3, categoryId = groceriesId),
                transaction(
                    budgetBookId = budgetBookId,
                    type = TransactionType.EXPENSE,
                    amountMinor = 1500,
                    occurredDate = LocalDate.of(2026, 5, 1),
                    now = now,
                ).copy(id = 4, categoryId = groceriesId),
            ),
        )

        val filtered = database.transactionDao().observeForHistoryFilter(
            budgetBookId = budgetBookId,
            startDateTime = LocalDate.of(2026, 4, 1).atStartOfDay(),
            endExclusiveDateTime = LocalDate.of(2026, 5, 1).atStartOfDay(),
            type = TransactionType.EXPENSE,
            filterByCategory = true,
            categoryIds = listOf(groceriesId),
        ).first()

        assertEquals(listOf(1L), filtered.map { it.id })
    }

    @Test
    fun observeHasTransactions_ignoresDateAndCategoryFilters() = runBlocking {
        val now = Instant.parse("2026-04-01T00:00:00Z")
        val budgetBookId = database.budgetBookDao().insert(
            BudgetBookEntity(title = "Personal", createdAt = now, updatedAt = now),
        )

        assertFalse(database.transactionDao().observeHasTransactions(budgetBookId).first())

        database.transactionDao().insert(
            transaction(
                budgetBookId = budgetBookId,
                type = TransactionType.EXPENSE,
                amountMinor = 2000,
                occurredDate = LocalDate.of(2026, 3, 15),
                now = now,
            ),
        )

        assertTrue(database.transactionDao().observeHasTransactions(budgetBookId).first())
    }

    @Test
    fun hotTransactionQueriesUseBudgetBookIndexes() {
        assertQueryPlanUsesIndex(
            sql = """
                EXPLAIN QUERY PLAN
                SELECT * FROM transactions
                WHERE budget_book_id = ?
                ORDER BY occurred_date DESC, id DESC
            """.trimIndent(),
            args = arrayOf(1L),
            expectedIndex = "index_transactions_budget_book_id_occurred_date_id",
        )
        assertQueryPlanUsesIndex(
            sql = """
                EXPLAIN QUERY PLAN
                SELECT COALESCE(SUM(amount_minor), 0) FROM transactions
                WHERE budget_book_id = ?
                AND type = ?
                AND occurred_date >= ?
                AND occurred_date < ?
            """.trimIndent(),
            args = arrayOf(1L, TransactionType.EXPENSE.name, "2026-04-01T00:00", "2026-05-01T00:00"),
            expectedIndex = "index_transactions_budget_book_id_type_occurred_date",
        )
        assertQueryPlanUsesIndex(
            sql = """
                EXPLAIN QUERY PLAN
                SELECT * FROM transactions
                WHERE budget_book_id = ?
                AND occurred_date >= ?
                AND occurred_date < ?
                ORDER BY occurred_date DESC, id DESC
            """.trimIndent(),
            args = arrayOf(1L, "2026-04-01T00:00", "2026-05-01T00:00"),
            expectedIndex = "index_transactions_budget_book_id_occurred_date_id",
        )
        assertQueryPlanUsesIndex(
            sql = """
                EXPLAIN QUERY PLAN
                SELECT * FROM transactions
                WHERE budget_book_id = ?
                AND occurred_date >= ?
                AND occurred_date < ?
                AND (? IS NULL OR type = ?)
                AND (? = 0 OR category_id IN (?))
                ORDER BY occurred_date DESC, id DESC
            """.trimIndent(),
            args = arrayOf(
                1L,
                "2026-04-01T00:00",
                "2026-05-01T00:00",
                TransactionType.EXPENSE.name,
                TransactionType.EXPENSE.name,
                1,
                10L,
            ),
            expectedIndex = "index_transactions_budget_book_id_occurred_date_id",
        )
        assertQueryPlanUsesIndex(
            sql = """
                EXPLAIN QUERY PLAN
                SELECT category_id, COALESCE(SUM(amount_minor), 0) AS total_minor
                FROM transactions
                WHERE budget_book_id = ?
                AND type = ?
                AND occurred_date >= ?
                AND occurred_date < ?
                GROUP BY category_id
                ORDER BY total_minor DESC
            """.trimIndent(),
            args = arrayOf(1L, TransactionType.EXPENSE.name, "2026-04-01T00:00", "2026-05-01T00:00"),
            expectedIndex = "index_transactions_budget_book_id_type_occurred_date",
        )
        assertQueryPlanUsesIndex(
            sql = """
                EXPLAIN QUERY PLAN
                SELECT substr(occurred_date, 1, 7) AS month, COALESCE(SUM(amount_minor), 0) AS total_minor
                FROM transactions
                WHERE budget_book_id = ?
                AND type = ?
                AND occurred_date >= ?
                AND occurred_date < ?
                GROUP BY month
                ORDER BY month
            """.trimIndent(),
            args = arrayOf(1L, TransactionType.EXPENSE.name, "2026-04-01T00:00", "2026-05-01T00:00"),
            expectedIndex = "index_transactions_budget_book_id_type_occurred_date",
        )
    }

    @Test
    fun recurringDueQueryUsesDueDateIndex() {
        assertQueryPlanUsesIndex(
            sql = """
                EXPLAIN QUERY PLAN
                SELECT * FROM recurring_transactions
                WHERE is_active = 1
                AND next_run_date <= ?
                AND (end_date IS NULL OR next_run_date <= end_date)
                ORDER BY next_run_date, id
            """.trimIndent(),
            args = arrayOf("2026-04-30"),
            expectedIndex = "index_recurring_transactions_is_active_next_run_date_id",
        )
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

    private fun category(
        budgetBookId: Long,
        title: String,
        now: Instant,
    ) = CategoryEntity(
        budgetBookId = budgetBookId,
        title = title,
        type = CategoryType.EXPENSE,
        iconName = "category",
        color = "#2E7D32",
        sortOrder = 0,
        createdAt = now,
        updatedAt = now,
    )

    private fun assertQueryPlanUsesIndex(
        sql: String,
        args: Array<Any>,
        expectedIndex: String,
    ) {
        val details = mutableListOf<String>()
        database.openHelper.writableDatabase.query(SimpleSQLiteQuery(sql, args)).use { cursor ->
            while (cursor.moveToNext()) {
                details += cursor.getString(3)
            }
        }
        assertTrue(
            "Expected query plan to use $expectedIndex, but was: $details",
            details.any { it.contains(expectedIndex) },
        )
    }
}
