package com.ozcomingfroo.mybudget.ui.onboarding

import android.content.Context
import android.content.res.Configuration
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.data.preferences.AppLanguageMode
import com.ozcomingfroo.mybudget.data.repository.StarterCategoryKey
import com.ozcomingfroo.mybudget.data.repository.StarterCategoryTitle
import java.util.Locale

@Suppress("DEPRECATION")
private val HebrewLocale = Locale("iw")

internal object StarterCategoryResources {
    fun resolveTitles(
        context: Context,
        languageMode: AppLanguageMode,
    ): List<StarterCategoryTitle> {
        val localizedContext = context.localizedFor(languageMode)
        return StarterCategoryKey.entries.map { key ->
            StarterCategoryTitle(
                key = key,
                title = localizedContext.getString(key.titleRes),
            )
        }
    }

    private fun Context.localizedFor(languageMode: AppLanguageMode): Context {
        val locale = when (languageMode) {
            AppLanguageMode.SYSTEM -> return this
            AppLanguageMode.EN_US -> Locale.US
            AppLanguageMode.HE -> HebrewLocale
        }
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        return createConfigurationContext(configuration)
    }

    private val StarterCategoryKey.titleRes: Int
        get() = when (this) {
            StarterCategoryKey.GROCERIES -> R.string.starter_category_groceries
            StarterCategoryKey.RESTAURANTS_COFFEE -> R.string.starter_category_restaurants_coffee
            StarterCategoryKey.RENT_MORTGAGE -> R.string.starter_category_rent_mortgage
            StarterCategoryKey.UTILITIES -> R.string.starter_category_utilities
            StarterCategoryKey.PHONE_INTERNET -> R.string.starter_category_phone_internet
            StarterCategoryKey.TRANSPORTATION -> R.string.starter_category_transportation
            StarterCategoryKey.HEALTH_MEDICAL -> R.string.starter_category_health_medical
            StarterCategoryKey.SHOPPING -> R.string.starter_category_shopping
            StarterCategoryKey.ENTERTAINMENT -> R.string.starter_category_entertainment
            StarterCategoryKey.OTHER_EXPENSE -> R.string.starter_category_other_expense
            StarterCategoryKey.SALARY -> R.string.starter_category_salary
            StarterCategoryKey.FREELANCE_BUSINESS -> R.string.starter_category_freelance_business
            StarterCategoryKey.TIPS_CASH -> R.string.starter_category_tips_cash
            StarterCategoryKey.REFUNDS -> R.string.starter_category_refunds
            StarterCategoryKey.GIFTS_RECEIVED -> R.string.starter_category_gifts_received
            StarterCategoryKey.OTHER_INCOME -> R.string.starter_category_other_income
        }
}
