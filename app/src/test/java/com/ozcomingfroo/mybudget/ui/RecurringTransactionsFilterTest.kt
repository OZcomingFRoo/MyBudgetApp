package com.ozcomingfroo.mybudget.ui

import com.ozcomingfroo.mybudget.data.local.entity.RecurringTransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.RecurringFrequency
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class RecurringTransactionsFilterTest {
    @Test
    fun `default filter returns all recurring transactions`() {
        val rules = listOf(
            rule(id = 1, type = TransactionType.EXPENSE),
            rule(id = 2, type = TransactionType.INCOME, isActive = false),
        )

        val filtered = rules.filterByRecurringTransactionsFilter(RecurringTransactionsFilter())

        assertEquals(listOf(1L, 2L), filtered.map { it.id })
    }

    @Test
    fun `filter applies type category frequency and status`() {
        val rules = listOf(
            rule(id = 1, categoryId = 10, frequency = RecurringFrequency.MONTHLY),
            rule(id = 2, categoryId = 20, frequency = RecurringFrequency.MONTHLY),
            rule(id = 3, categoryId = 10, frequency = RecurringFrequency.WEEKLY),
            rule(id = 4, categoryId = 10, frequency = RecurringFrequency.MONTHLY, isActive = false),
            rule(id = 5, categoryId = 10, frequency = RecurringFrequency.MONTHLY, type = TransactionType.INCOME),
        )

        val filtered = rules.filterByRecurringTransactionsFilter(
            RecurringTransactionsFilter(
                type = RecurringTransactionTypeFilter.Expense,
                selectedCategoryIds = setOf(10),
                selectedFrequencies = setOf(RecurringFrequency.MONTHLY),
                status = RecurringStatusFilter.Active,
            ),
        )

        assertEquals(listOf(1L), filtered.map { it.id })
    }

    @Test
    fun `filter includes inclusive next run date range`() {
        val rules = listOf(
            rule(id = 1, nextRunDate = LocalDate.of(2026, 7, 1)),
            rule(id = 2, nextRunDate = LocalDate.of(2026, 7, 31)),
            rule(id = 3, nextRunDate = LocalDate.of(2026, 8, 1)),
        )

        val filtered = rules.filterByRecurringTransactionsFilter(
            RecurringTransactionsFilter(
                nextRunStartDate = LocalDate.of(2026, 7, 1),
                nextRunEndDate = LocalDate.of(2026, 7, 31),
            ),
        )

        assertEquals(listOf(1L, 2L), filtered.map { it.id })
    }

    @Test
    fun `base title prefers rule title then category then uncategorized`() {
        assertEquals(
            "Rent",
            recurringBaseTitle(rule(title = "Rent"), categoryTitle = "Housing", uncategorized = "Uncategorized"),
        )
        assertEquals(
            "Housing",
            recurringBaseTitle(rule(title = null), categoryTitle = "Housing", uncategorized = "Uncategorized"),
        )
        assertEquals(
            "Uncategorized",
            recurringBaseTitle(rule(title = null), categoryTitle = null, uncategorized = "Uncategorized"),
        )
    }

    private fun rule(
        id: Long = 1,
        categoryId: Long? = null,
        type: TransactionType = TransactionType.EXPENSE,
        frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
        nextRunDate: LocalDate = LocalDate.of(2026, 7, 1),
        isActive: Boolean = true,
        title: String? = null,
    ) = RecurringTransactionEntity(
        id = id,
        budgetBookId = 1,
        categoryId = categoryId,
        type = type,
        amountMinor = 1000,
        title = title,
        frequency = frequency,
        interval = 1,
        startDate = LocalDate.of(2026, 6, 1),
        nextRunDate = nextRunDate,
        isActive = isActive,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )
}
