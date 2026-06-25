package com.ozcomingfroo.mybudget.ui

import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportsTrendBucketTest {
    @Test
    fun `weekly report buckets transactions by day in one selected range`() {
        val range = ReportsDateRange(
            startDate = LocalDate.of(2026, 6, 14),
            endExclusiveDate = LocalDate.of(2026, 6, 21),
        )
        val buckets = range.buildTrendBuckets(
            transactions = listOf(
                transaction(date = LocalDate.of(2026, 6, 14), type = TransactionType.INCOME, amountMinor = 1000),
                transaction(date = LocalDate.of(2026, 6, 14), type = TransactionType.EXPENSE, amountMinor = 250),
                transaction(date = LocalDate.of(2026, 6, 20), type = TransactionType.EXPENSE, amountMinor = 400),
            ),
            type = ReportsRangeType.Week,
            locale = Locale.US,
        )

        assertEquals(7, buckets.size)
        assertEquals(1000L, buckets.first().incomeMinor)
        assertEquals(250L, buckets.first().expenseMinor)
        assertEquals(400L, buckets.last().expenseMinor)
    }

    @Test
    fun `custom report switches to monthly buckets for long ranges`() {
        val range = ReportsDateRange(
            startDate = LocalDate.of(2026, 1, 1),
            endExclusiveDate = LocalDate.of(2026, 5, 1),
        )
        val buckets = range.buildTrendBuckets(
            transactions = listOf(
                transaction(date = LocalDate.of(2026, 1, 31), type = TransactionType.EXPENSE, amountMinor = 100),
                transaction(date = LocalDate.of(2026, 4, 30), type = TransactionType.INCOME, amountMinor = 500),
            ),
            type = ReportsRangeType.Custom,
            locale = Locale.US,
        )

        assertEquals(listOf("1/1", "2/1", "3/1", "4/1"), buckets.map { it.label })
        assertEquals(100L, buckets[0].expenseMinor)
        assertEquals(500L, buckets[3].incomeMinor)
    }

    private fun transaction(
        date: LocalDate,
        type: TransactionType,
        amountMinor: Long,
    ) = TransactionEntity(
        budgetBookId = 1,
        type = type,
        amountMinor = amountMinor,
        occurredAt = LocalDateTime.of(date, java.time.LocalTime.NOON),
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )
}
