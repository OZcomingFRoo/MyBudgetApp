package com.ozcomingfroo.mybudget.domain.recurring

import com.ozcomingfroo.mybudget.data.local.entity.RecurringTransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.RecurringFrequency
import java.time.LocalDate

object RecurrenceCalculator {
    fun nextDate(rule: RecurringTransactionEntity, fromDate: LocalDate = rule.nextRunDate): LocalDate {
        require(rule.interval >= 1) { "Recurring transaction interval must be at least 1." }

        return when (rule.frequency) {
            RecurringFrequency.DAILY -> fromDate.plusDays(rule.interval.toLong())
            RecurringFrequency.WEEKLY -> fromDate.plusWeeks(rule.interval.toLong())
            RecurringFrequency.MONTHLY -> fromDate.plusMonthsPreservingAnchor(
                monthsToAdd = rule.interval.toLong(),
                anchorDay = rule.scheduleMonthDay ?: rule.startDate.dayOfMonth,
            )
            RecurringFrequency.YEARLY -> fromDate.plusYearsPreservingAnchor(
                yearsToAdd = rule.interval.toLong(),
                anchorMonth = rule.startDate.monthValue,
                anchorDay = rule.startDate.dayOfMonth,
            )
        }
    }

    private fun LocalDate.plusMonthsPreservingAnchor(
        monthsToAdd: Long,
        anchorDay: Int,
    ): LocalDate {
        val targetMonth = withDayOfMonth(1).plusMonths(monthsToAdd)
        val safeDay = minOf(anchorDay, targetMonth.lengthOfMonth())
        return targetMonth.withDayOfMonth(safeDay)
    }

    private fun LocalDate.plusYearsPreservingAnchor(
        yearsToAdd: Long,
        anchorMonth: Int,
        anchorDay: Int,
    ): LocalDate {
        val targetYear = year + yearsToAdd.toInt()
        val targetMonth = LocalDate.of(targetYear, anchorMonth, 1)
        val safeDay = minOf(anchorDay, targetMonth.lengthOfMonth())
        return targetMonth.withDayOfMonth(safeDay)
    }
}
