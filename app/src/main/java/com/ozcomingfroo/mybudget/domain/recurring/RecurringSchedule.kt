package com.ozcomingfroo.mybudget.domain.recurring

import com.ozcomingfroo.mybudget.data.local.model.RecurringFrequency
import java.time.DayOfWeek
import java.time.LocalDate

object RecurringSchedule {
    const val MinInterval = 1
    const val MaxSimpleInterval = 3
    const val MinMonthDay = 1
    const val MaxMonthDay = 30

    fun nextStartDate(
        frequency: RecurringFrequency,
        today: LocalDate,
        scheduleWeekday: Int?,
        scheduleMonthDay: Int?,
    ): LocalDate = when (frequency) {
        RecurringFrequency.DAILY -> today
        RecurringFrequency.WEEKLY -> nextWeekday(
            today = today,
            isoWeekday = scheduleWeekday ?: today.dayOfWeek.value,
        )
        RecurringFrequency.MONTHLY -> nextMonthDay(
            today = today,
            monthDay = scheduleMonthDay ?: today.dayOfMonth.coerceAtMost(MaxMonthDay),
        )
        RecurringFrequency.YEARLY -> today
    }

    fun normalizedWeekday(frequency: RecurringFrequency, weekday: Int?): Int? =
        if (frequency == RecurringFrequency.WEEKLY) {
            (weekday ?: DayOfWeek.MONDAY.value).coerceIn(DayOfWeek.MONDAY.value, DayOfWeek.SUNDAY.value)
        } else {
            null
        }

    fun normalizedMonthDay(frequency: RecurringFrequency, monthDay: Int?): Int? =
        if (frequency == RecurringFrequency.MONTHLY) {
            (monthDay ?: 1).coerceIn(MinMonthDay, MaxMonthDay)
        } else {
            null
        }

    private fun nextWeekday(today: LocalDate, isoWeekday: Int): LocalDate {
        val target = isoWeekday.coerceIn(DayOfWeek.MONDAY.value, DayOfWeek.SUNDAY.value)
        val daysAhead = (target - today.dayOfWeek.value + 7) % 7
        return today.plusDays(daysAhead.toLong())
    }

    private fun nextMonthDay(today: LocalDate, monthDay: Int): LocalDate {
        val targetDay = monthDay.coerceIn(MinMonthDay, MaxMonthDay)
        val thisMonth = today.withClampedDay(targetDay)
        return if (thisMonth >= today) {
            thisMonth
        } else {
            today.plusMonths(1).withClampedDay(targetDay)
        }
    }

    private fun LocalDate.withClampedDay(day: Int): LocalDate {
        val monthStart = withDayOfMonth(1)
        return monthStart.withDayOfMonth(minOf(day, monthStart.lengthOfMonth()))
    }
}
