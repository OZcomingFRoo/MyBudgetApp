package com.ozcomingfroo.mybudget.core.money

object MoneyFormatter {
    private val validMoneyInput = Regex("""^\d+(\.\d{1,2})?$""")

    fun parseAmountMinor(input: String): Long {
        val normalized = input.trim()
        require(validMoneyInput.matches(normalized)) {
            "Amount must be a non-negative number with up to 2 decimal digits."
        }

        val parts = normalized.split('.', limit = 2)
        val major = parts[0].toLong()
        val minor = when (parts.size) {
            1 -> 0
            else -> parts[1].padEnd(2, '0').toInt()
        }

        return Math.addExact(Math.multiplyExact(major, 100L), minor.toLong())
    }

    fun formatAmount(amountMinor: Long): String {
        val sign = if (amountMinor < 0) "-" else ""
        val absoluteAmount = kotlin.math.abs(amountMinor)
        val major = absoluteAmount / 100
        val minor = absoluteAmount % 100
        return "$sign$major.${minor.toString().padStart(2, '0')}"
    }
}
