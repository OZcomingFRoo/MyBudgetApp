package com.ozcomingfroo.mybudget.data.repository

import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import java.time.Instant

object StarterCategories {
    fun createForBudgetBook(budgetBookId: Long, now: Instant): List<CategoryEntity> {
        val expenseCategories = listOf(
            StarterCategory("Groceries", "shopping_cart", "#2E7D32"),
            StarterCategory("Restaurants & Coffee", "restaurant", "#C2410C"),
            StarterCategory("Rent / Mortgage", "home", "#334155"),
            StarterCategory("Utilities", "receipt", "#0891B2"),
            StarterCategory("Phone & Internet", "phone_android", "#2563EB"),
            StarterCategory("Transportation", "directions_car", "#7C3AED"),
            StarterCategory("Health & Medical", "medical_services", "#DC2626"),
            StarterCategory("Shopping", "shopping_bag", "#4F46E5"),
            StarterCategory("Entertainment", "movie", "#DB2777"),
            StarterCategory("Other Expense", "category", "#64748B"),
        )
        val incomeCategories = listOf(
            StarterCategory("Salary", "payments", "#15803D"),
            StarterCategory("Freelance / Business", "work", "#0F766E"),
            StarterCategory("Tips / Cash", "attach_money", "#0369A1"),
            StarterCategory("Refunds", "undo", "#65A30D"),
            StarterCategory("Gifts Received", "card_giftcard", "#BE185D"),
            StarterCategory("Other Income", "category", "#64748B"),
        )

        return expenseCategories.mapIndexed { index, category ->
            category.toEntity(budgetBookId, CategoryType.EXPENSE, index, now)
        } + incomeCategories.mapIndexed { index, category ->
            category.toEntity(budgetBookId, CategoryType.INCOME, expenseCategories.size + index, now)
        }
    }

    private data class StarterCategory(
        val title: String,
        val iconName: String,
        val color: String,
    ) {
        fun toEntity(
            budgetBookId: Long,
            type: CategoryType,
            sortOrder: Int,
            now: Instant,
        ) = CategoryEntity(
            budgetBookId = budgetBookId,
            title = title,
            type = type,
            iconName = iconName,
            color = color,
            sortOrder = sortOrder,
            createdAt = now,
            updatedAt = now,
        )
    }
}
