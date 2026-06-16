package com.ozcomingfroo.mybudget.ui

import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardDateRangeTest {
    @Test
    fun monthRangeStartsAtFirstDayAndEndsAtNextMonth() {
        val range = DashboardDateRange.from(
            type = DashboardRangeType.Month,
            anchorDate = LocalDate.of(2026, 6, 16),
            locale = Locale.US,
        )

        assertEquals(LocalDate.of(2026, 6, 1), range.startDate)
        assertEquals(LocalDate.of(2026, 7, 1), range.endExclusiveDate)
    }

    @Test
    fun weekRangeUsesLocaleFirstDay() {
        val range = DashboardDateRange.from(
            type = DashboardRangeType.Week,
            anchorDate = LocalDate.of(2026, 6, 16),
            locale = Locale.US,
        )

        assertEquals(LocalDate.of(2026, 6, 14), range.startDate)
        assertEquals(LocalDate.of(2026, 6, 21), range.endExclusiveDate)
    }

    @Test
    fun yearRangeCoversCalendarYear() {
        val range = DashboardDateRange.from(
            type = DashboardRangeType.Year,
            anchorDate = LocalDate.of(2026, 6, 16),
            locale = Locale.US,
        )

        assertEquals(LocalDate.of(2026, 1, 1), range.startDate)
        assertEquals(LocalDate.of(2027, 1, 1), range.endExclusiveDate)
    }
}
