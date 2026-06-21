package com.ozcomingfroo.mybudget.data.repository

import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StarterCategoriesTest {
    @Test
    fun createForBudgetBook_returnsAgreedStarterMetadataWithEnglishFallbackTitles() {
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
        assertEquals((0..15).toList(), categories.map { it.sortOrder })
        assertEquals(
            listOf(
                "shopping_cart",
                "restaurant",
                "home",
                "receipt",
                "phone_android",
                "directions_car",
                "medical_services",
                "shopping_bag",
                "movie",
                "category",
                "payments",
                "work",
                "attach_money",
                "undo",
                "card_giftcard",
                "category",
            ),
            categories.map { it.iconName },
        )
    }

    @Test
    fun createForBudgetBook_usesSuppliedLocalizedTitles() {
        val categories = StarterCategories.createForBudgetBook(
            budgetBookId = 7L,
            now = Instant.parse("2026-01-01T00:00:00Z"),
            titles = StarterCategories.englishTitles().map { title ->
                title.copy(title = "Localized ${title.key.name}")
            },
        )

        assertEquals(
            "Localized GROCERIES",
            categories.first { it.sortOrder == 0 }.title,
        )
        assertEquals(
            "Localized SALARY",
            categories.first { it.sortOrder == 10 }.title,
        )
    }

    @Test
    fun matchingRelocalizedCategory_updatesStarterButIgnoresCustomTitle() {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val updateTime = Instant.parse("2026-01-02T00:00:00Z")
        val starterCategory = StarterCategories.createForBudgetBook(
            budgetBookId = 7L,
            now = now,
        ).first { it.sortOrder == 0 }
        val localizedTitles = StarterCategories.englishTitles().map { title ->
            if (title.key == StarterCategoryKey.GROCERIES) {
                title.copy(title = "Localized groceries")
            } else {
                title
            }
        }

        val localized = StarterCategories.matchingRelocalizedCategory(
            category = starterCategory,
            localizedTitles = localizedTitles,
            now = updateTime,
        )
        val custom = StarterCategories.matchingRelocalizedCategory(
            category = starterCategory.copy(title = "My custom groceries"),
            localizedTitles = localizedTitles,
            now = updateTime,
        )

        assertEquals("Localized groceries", localized?.title)
        assertEquals(updateTime, localized?.updatedAt)
        assertNull(custom)
    }
}
