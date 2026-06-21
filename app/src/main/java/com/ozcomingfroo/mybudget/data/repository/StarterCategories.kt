package com.ozcomingfroo.mybudget.data.repository

import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import java.time.Instant

enum class StarterCategoryKey {
    GROCERIES,
    RESTAURANTS_COFFEE,
    RENT_MORTGAGE,
    UTILITIES,
    PHONE_INTERNET,
    TRANSPORTATION,
    HEALTH_MEDICAL,
    SHOPPING,
    ENTERTAINMENT,
    OTHER_EXPENSE,
    SALARY,
    FREELANCE_BUSINESS,
    TIPS_CASH,
    REFUNDS,
    GIFTS_RECEIVED,
    OTHER_INCOME,
}

data class StarterCategoryTitle(
    val key: StarterCategoryKey,
    val title: String,
)

object StarterCategories {
    fun createForBudgetBook(
        budgetBookId: Long,
        now: Instant,
        titles: List<StarterCategoryTitle> = englishTitles(),
    ): List<CategoryEntity> {
        val titleByKey = titles.associate { it.key to it.title }
        return definitions.map { definition ->
            definition.toEntity(
                budgetBookId = budgetBookId,
                title = titleByKey[definition.key] ?: definition.englishTitle,
                now = now,
            )
        }
    }

    fun englishTitles(): List<StarterCategoryTitle> =
        definitions.map { definition ->
            StarterCategoryTitle(
                key = definition.key,
                title = definition.englishTitle,
            )
        }

    fun matchingRelocalizedCategory(
        category: CategoryEntity,
        localizedTitles: List<StarterCategoryTitle>,
        now: Instant,
    ): CategoryEntity? {
        val definition = category.expectedStarterCategory(localizedTitles) ?: return null
        val titleByKey = localizedTitles.associate { it.key to it.title }
        val localizedTitle = titleByKey[definition.key] ?: definition.englishTitle
        if (category.title == localizedTitle) return null

        return category.copy(
            title = localizedTitle,
            updatedAt = now,
        )
    }

    private fun CategoryEntity.expectedStarterCategory(
        localizedTitles: List<StarterCategoryTitle>,
    ): StarterCategoryDefinition? {
        val definition = definitions.firstOrNull { definition ->
            definition.type == type && definition.sortOrder == sortOrder
        } ?: return null
        val localizedTitle = localizedTitles.firstOrNull { it.key == definition.key }?.title

        if (iconName != definition.iconName || color != definition.color) return null
        if (title != definition.englishTitle && title != localizedTitle) return null

        return definition
    }

    private val definitions = listOf(
        StarterCategoryDefinition(
            key = StarterCategoryKey.GROCERIES,
            englishTitle = "Groceries",
            type = CategoryType.EXPENSE,
            iconName = "shopping_cart",
            color = "#2E7D32",
            sortOrder = 0,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.RESTAURANTS_COFFEE,
            englishTitle = "Restaurants & Coffee",
            type = CategoryType.EXPENSE,
            iconName = "restaurant",
            color = "#C2410C",
            sortOrder = 1,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.RENT_MORTGAGE,
            englishTitle = "Rent / Mortgage",
            type = CategoryType.EXPENSE,
            iconName = "home",
            color = "#334155",
            sortOrder = 2,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.UTILITIES,
            englishTitle = "Utilities",
            type = CategoryType.EXPENSE,
            iconName = "receipt",
            color = "#0891B2",
            sortOrder = 3,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.PHONE_INTERNET,
            englishTitle = "Phone & Internet",
            type = CategoryType.EXPENSE,
            iconName = "phone_android",
            color = "#2563EB",
            sortOrder = 4,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.TRANSPORTATION,
            englishTitle = "Transportation",
            type = CategoryType.EXPENSE,
            iconName = "directions_car",
            color = "#7C3AED",
            sortOrder = 5,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.HEALTH_MEDICAL,
            englishTitle = "Health & Medical",
            type = CategoryType.EXPENSE,
            iconName = "medical_services",
            color = "#DC2626",
            sortOrder = 6,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.SHOPPING,
            englishTitle = "Shopping",
            type = CategoryType.EXPENSE,
            iconName = "shopping_bag",
            color = "#4F46E5",
            sortOrder = 7,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.ENTERTAINMENT,
            englishTitle = "Entertainment",
            type = CategoryType.EXPENSE,
            iconName = "movie",
            color = "#DB2777",
            sortOrder = 8,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.OTHER_EXPENSE,
            englishTitle = "Other Expense",
            type = CategoryType.EXPENSE,
            iconName = "category",
            color = "#64748B",
            sortOrder = 9,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.SALARY,
            englishTitle = "Salary",
            type = CategoryType.INCOME,
            iconName = "payments",
            color = "#15803D",
            sortOrder = 10,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.FREELANCE_BUSINESS,
            englishTitle = "Freelance / Business",
            type = CategoryType.INCOME,
            iconName = "work",
            color = "#0F766E",
            sortOrder = 11,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.TIPS_CASH,
            englishTitle = "Tips / Cash",
            type = CategoryType.INCOME,
            iconName = "attach_money",
            color = "#0369A1",
            sortOrder = 12,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.REFUNDS,
            englishTitle = "Refunds",
            type = CategoryType.INCOME,
            iconName = "undo",
            color = "#65A30D",
            sortOrder = 13,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.GIFTS_RECEIVED,
            englishTitle = "Gifts Received",
            type = CategoryType.INCOME,
            iconName = "card_giftcard",
            color = "#BE185D",
            sortOrder = 14,
        ),
        StarterCategoryDefinition(
            key = StarterCategoryKey.OTHER_INCOME,
            englishTitle = "Other Income",
            type = CategoryType.INCOME,
            iconName = "category",
            color = "#64748B",
            sortOrder = 15,
        ),
    )

    private data class StarterCategoryDefinition(
        val key: StarterCategoryKey,
        val englishTitle: String,
        val type: CategoryType,
        val iconName: String,
        val color: String,
        val sortOrder: Int,
    ) {
        fun toEntity(
            budgetBookId: Long,
            title: String,
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
