package com.ozcomingfroo.mybudget.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceWidgetUpdateNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun notifyWidgetsChanged() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, BalanceWidgetProvider::class.java),
        )
        if (appWidgetIds.isEmpty()) return

        val intent = Intent(context, BalanceWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(intent)
    }
}
