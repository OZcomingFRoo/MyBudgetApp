package com.ozcomingfroo.mybudget.domain.recurring

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
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecurringTransactionGeneratorTest {
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
    fun generateDue_createsAllMissedTransactionsAndAdvancesRule() = runBlocking {
        val clock = Clock.fixed(
            Instant.parse("2026-04-30T08:00:00Z"),
            ZoneId.of("UTC"),
        )
        val generator = RecurringTransactionGenerator(
            database = database,
            recurringTransactionDao = database.recurringTransactionDao(),
            transactionDao = database.transactionDao(),
            clock = clock,
        )
        val now = clock.instant()
        val budgetBookId = database.budgetBookDao().insert(
            BudgetBookEntity(title = "Personal", createdAt = now, updatedAt = now),
        )
        val ruleId = database.recurringTransactionDao().insert(
            RecurringTransactionEntity(
                budgetBookId = budgetBookId,
                type = TransactionType.EXPENSE,
                amountMinor = 450000,
                title = "Rent",
                frequency = RecurringFrequency.MONTHLY,
                interval = 1,
                startDate = LocalDate.of(2026, 1, 31),
                nextRunDate = LocalDate.of(2026, 1, 31),
                createdAt = now,
                updatedAt = now,
            ),
        )

        val generatedCount = generator.generateDue(LocalDate.of(2026, 4, 30))

        val generatedTransactions = database.transactionDao().getForBudgetBook(budgetBookId)
        val updatedRule = database.recurringTransactionDao().getById(ruleId)

        assertEquals(4, generatedCount)
        assertEquals(
            listOf(
                LocalDate.of(2026, 1, 31),
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2026, 3, 31),
                LocalDate.of(2026, 4, 30),
            ),
            generatedTransactions.map { it.occurredDate },
        )
        assertEquals(listOf(ruleId, ruleId, ruleId, ruleId), generatedTransactions.map { it.recurringTransactionId })
        assertEquals(LocalDate.of(2026, 4, 30), updatedRule?.lastRunDate)
        assertEquals(LocalDate.of(2026, 5, 31), updatedRule?.nextRunDate)
    }
}
