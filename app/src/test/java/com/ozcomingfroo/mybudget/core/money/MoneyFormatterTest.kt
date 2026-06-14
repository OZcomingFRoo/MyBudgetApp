package com.ozcomingfroo.mybudget.core.money

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyFormatterTest {
    @Test
    fun parseAmountMinor_acceptsWholeAndDecimalValues() {
        assertEquals(1000L, MoneyFormatter.parseAmountMinor("10"))
        assertEquals(1050L, MoneyFormatter.parseAmountMinor("10.5"))
        assertEquals(1055L, MoneyFormatter.parseAmountMinor("10.55"))
        assertEquals(1L, MoneyFormatter.parseAmountMinor("0.01"))
    }

    @Test
    fun parseAmountMinor_rejectsMoreThanTwoDecimalDigits() {
        assertThrows(IllegalArgumentException::class.java) {
            MoneyFormatter.parseAmountMinor("10.555")
        }
    }

    @Test
    fun formatAmount_formatsMinorUnitsAtDisplayBoundary() {
        assertEquals("10.55", MoneyFormatter.formatAmount(1055L))
        assertEquals("-10.55", MoneyFormatter.formatAmount(-1055L))
    }
}
