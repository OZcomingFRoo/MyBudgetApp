package com.example.mybudget.data.repository

import com.example.mybudget.data.local.model.CategoryType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class StarterCategoriesTest {
    @Test
    fun createForBudgetBook_returnsAgreedStarterCategories() {
        val categories = StarterCategories.createForBudgetBook(
            budgetBookId = 7L,
            now = Instant.parse("2026-01-01T00:00:00Z"),
        )

        val expenseTitles = categories
            .filter { it.type == CategoryType.EXPENSE }
            .map { it.title }
        val incomeTitles = categories
            .filter { it.type == CategoryType.INCOME }
            .map { it.title }

        assertEquals(
            listOf(
                "Groceries",
                "Restaurants & Coffee",
                "Rent / Mortgage",
                "Utilities",
                "Phone & Internet",
                "Transportation",
                "Health & Medical",
                "Shopping",
                "Entertainment",
                "Other Expense",
            ),
            expenseTitles,
        )
        assertEquals(
            listOf(
                "Salary",
                "Freelance / Business",
                "Tips / Cash",
                "Refunds",
                "Gifts Received",
                "Other Income",
            ),
            incomeTitles,
        )
    }
}
