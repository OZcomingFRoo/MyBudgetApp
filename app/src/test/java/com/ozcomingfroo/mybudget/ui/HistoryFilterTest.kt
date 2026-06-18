package com.ozcomingfroo.mybudget.ui

import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryFilterTest {
    @Test
    fun `filter includes inclusive date range`() {
        val transactions = listOf(
            transaction(id = 1, date = LocalDate.of(2026, 6, 1)),
            transaction(id = 2, date = LocalDate.of(2026, 6, 30)),
            transaction(id = 3, date = LocalDate.of(2026, 7, 1)),
        )

        val filtered = transactions.filterByHistoryFilter(
            HistoryFilter(
                startDate = LocalDate.of(2026, 6, 1),
                endDate = LocalDate.of(2026, 6, 30),
                type = HistoryTransactionTypeFilter.Both,
                selectedCategoryIds = emptySet(),
            ),
        )

        assertEquals(listOf(1L, 2L), filtered.map { it.id })
    }

    @Test
    fun `filter applies transaction type and category selections`() {
        val transactions = listOf(
            transaction(id = 1, type = TransactionType.EXPENSE, categoryId = 10),
            transaction(id = 2, type = TransactionType.EXPENSE, categoryId = 20),
            transaction(id = 3, type = TransactionType.INCOME, categoryId = 10),
        )

        val filtered = transactions.filterByHistoryFilter(
            HistoryFilter(
                startDate = LocalDate.of(2026, 6, 1),
                endDate = LocalDate.of(2026, 6, 30),
                type = HistoryTransactionTypeFilter.Expense,
                selectedCategoryIds = setOf(10),
            ),
        )

        assertEquals(listOf(1L), filtered.map { it.id })
    }

    private fun transaction(
        id: Long,
        date: LocalDate = LocalDate.of(2026, 6, 15),
        type: TransactionType = TransactionType.EXPENSE,
        categoryId: Long? = null,
    ): TransactionEntity =
        TransactionEntity(
            id = id,
            budgetBookId = 1,
            categoryId = categoryId,
            type = type,
            amountMinor = 100,
            occurredAt = LocalDateTime.of(date, java.time.LocalTime.NOON),
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        )
}
