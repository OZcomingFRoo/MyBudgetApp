package com.ozcomingfroo.mybudget.domain.recurring

import com.ozcomingfroo.mybudget.data.local.entity.RecurringTransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.RecurringFrequency
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class RecurrenceCalculatorTest {
    @Test
    fun nextDate_monthlyPreservesOriginalAnchorDayAtMonthEnd() {
        val rule = recurringRule(
            frequency = RecurringFrequency.MONTHLY,
            startDate = LocalDate.of(2026, 1, 31),
            nextRunDate = LocalDate.of(2026, 1, 31),
        )

        val february = RecurrenceCalculator.nextDate(rule, LocalDate.of(2026, 1, 31))
        val march = RecurrenceCalculator.nextDate(rule, february)
        val april = RecurrenceCalculator.nextDate(rule, march)

        assertEquals(LocalDate.of(2026, 2, 28), february)
        assertEquals(LocalDate.of(2026, 3, 31), march)
        assertEquals(LocalDate.of(2026, 4, 30), april)
    }

    @Test
    fun nextDate_monthlyUsesStoredScheduleMonthDayWhenStartDateWasClamped() {
        val rule = recurringRule(
            frequency = RecurringFrequency.MONTHLY,
            startDate = LocalDate.of(2026, 2, 28),
            nextRunDate = LocalDate.of(2026, 2, 28),
            scheduleMonthDay = 30,
        )

        val march = RecurrenceCalculator.nextDate(rule, LocalDate.of(2026, 2, 28))
        val april = RecurrenceCalculator.nextDate(rule, march)

        assertEquals(LocalDate.of(2026, 3, 30), march)
        assertEquals(LocalDate.of(2026, 4, 30), april)
    }

    private fun recurringRule(
        frequency: RecurringFrequency,
        startDate: LocalDate,
        nextRunDate: LocalDate,
        scheduleMonthDay: Int? = null,
    ): RecurringTransactionEntity {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return RecurringTransactionEntity(
            id = 1,
            budgetBookId = 1,
            type = TransactionType.EXPENSE,
            amountMinor = 1000,
            frequency = frequency,
            interval = 1,
            scheduleMonthDay = scheduleMonthDay,
            startDate = startDate,
            nextRunDate = nextRunDate,
            createdAt = now,
            updatedAt = now,
        )
    }
}
