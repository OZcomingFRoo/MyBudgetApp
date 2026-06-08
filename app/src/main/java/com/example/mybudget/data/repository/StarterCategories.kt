package com.example.mybudget.data.repository

import com.example.mybudget.data.local.entity.CategoryEntity
import com.example.mybudget.data.local.model.CategoryType
import java.time.Instant

object StarterCategories {
    fun createForBudgetBook(budgetBookId: Long, now: Instant): List<CategoryEntity> {
        val expenseCategories = listOf(
            StarterCategory("Groceries", "shopping_cart", "#2E7D32"),
            StarterCategory("Restaurants", "restaurant", "#C2410C"),
            StarterCategory("Transport", "directions_car", "#2563EB"),
            StarterCategory("Fuel", "local_gas_station", "#7C3AED"),
            StarterCategory("Rent", "home", "#334155"),
            StarterCategory("Utilities", "receipt", "#0891B2"),
            StarterCategory("Health", "medical_services", "#DC2626"),
            StarterCategory("Entertainment", "movie", "#DB2777"),
            StarterCategory("Shopping", "shopping_bag", "#4F46E5"),
            StarterCategory("Other", "category", "#64748B"),
        )
        val incomeCategories = listOf(
            StarterCategory("Paycheck", "payments", "#15803D"),
            StarterCategory("Freelance", "work", "#0F766E"),
            StarterCategory("Sale", "sell", "#0369A1"),
            StarterCategory("Refund", "undo", "#65A30D"),
            StarterCategory("Gift", "card_giftcard", "#BE185D"),
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
