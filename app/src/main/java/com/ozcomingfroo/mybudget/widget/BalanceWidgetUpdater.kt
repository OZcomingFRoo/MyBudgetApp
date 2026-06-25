package com.ozcomingfroo.mybudget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.widget.RemoteViews
import com.ozcomingfroo.mybudget.MainActivity
import com.ozcomingfroo.mybudget.MyBudgetIntents
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.core.money.MoneyFormatter
import com.ozcomingfroo.mybudget.data.local.dao.BudgetBookDao
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.preferences.AppLanguageMode
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import com.ozcomingfroo.mybudget.data.preferences.AppThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceWidgetUpdater @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val budgetBookDao: BudgetBookDao,
    private val appPreferencesRepository: AppPreferencesRepository,
) {
    suspend fun update(appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val preferences = appPreferencesRepository.getPreferences()
        val localizedContext = context.localizedFor(preferences.languageMode)
        val widgetTheme = context.widgetThemeFor(preferences.themeMode)
        val budgetBook = budgetBookDao.getWidgetCandidate(preferences.selectedBudgetBookId)
        val views = createViews(localizedContext, budgetBook, widgetTheme)

        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun createViews(
        localizedContext: Context,
        budgetBook: BudgetBookEntity?,
        widgetTheme: WidgetTheme,
    ): RemoteViews {
        val balanceMinor = budgetBook?.let { it.totalIncomeMinor - it.totalExpenseMinor } ?: 0L
        return RemoteViews(context.packageName, R.layout.widget_balance).apply {
            setInt(R.id.widget_root, "setBackgroundResource", widgetTheme.backgroundRes)
            setTextColor(R.id.widget_title, widgetTheme.primaryTextColor)
            setTextColor(R.id.widget_amount, widgetTheme.primaryTextColor)
            setTextColor(R.id.widget_add_transaction, widgetTheme.accentTextColor)
            setImageViewResource(R.id.widget_icon, R.mipmap.ic_launcher_round)
            setTextViewText(R.id.widget_title, localizedContext.getString(R.string.widget_balance_title))
            setTextViewText(R.id.widget_amount, MoneyFormatter.formatAmount(balanceMinor))
            setTextViewText(R.id.widget_add_transaction, localizedContext.getString(R.string.widget_add_short))
            setOnClickPendingIntent(R.id.widget_root, dashboardPendingIntent())
            setOnClickPendingIntent(R.id.widget_add_transaction, addTransactionPendingIntent())
        }
    }

    private fun dashboardPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = MyBudgetIntents.ACTION_OPEN_DASHBOARD
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            DashboardRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun addTransactionPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = MyBudgetIntents.ACTION_OPEN_ADD_TRANSACTION
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            AddTransactionRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun Context.localizedFor(languageMode: AppLanguageMode): Context {
        if (languageMode == AppLanguageMode.SYSTEM) return this

        val locale = when (languageMode) {
            AppLanguageMode.SYSTEM -> Locale.getDefault()
            AppLanguageMode.EN_US -> Locale.US
            AppLanguageMode.HE -> HebrewLocale
        }
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        return createConfigurationContext(configuration)
    }

    private fun Context.widgetThemeFor(themeMode: AppThemeMode): WidgetTheme {
        val useNightTheme = when (themeMode) {
            AppThemeMode.DEFAULT -> false
            AppThemeMode.NIGHT -> true
            AppThemeMode.SYSTEM -> {
                val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightMode == Configuration.UI_MODE_NIGHT_YES
            }
        }
        return if (useNightTheme) {
            WidgetTheme(
                backgroundRes = R.drawable.widget_balance_background_night,
                primaryTextColor = Color.rgb(231, 238, 232),
                accentTextColor = Color.rgb(231, 184, 75),
            )
        } else {
            WidgetTheme(
                backgroundRes = R.drawable.widget_balance_background,
                primaryTextColor = Color.WHITE,
                accentTextColor = Color.rgb(231, 184, 75),
            )
        }
    }

    private data class WidgetTheme(
        val backgroundRes: Int,
        val primaryTextColor: Int,
        val accentTextColor: Int,
    )

    private companion object {
        private const val DashboardRequestCode = 2000
        private const val AddTransactionRequestCode = 2001

        @Suppress("DEPRECATION")
        private val HebrewLocale = Locale("iw")
    }
}
