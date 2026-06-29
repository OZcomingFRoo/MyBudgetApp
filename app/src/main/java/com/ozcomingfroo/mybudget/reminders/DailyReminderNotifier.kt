package com.ozcomingfroo.mybudget.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ozcomingfroo.mybudget.MainActivity
import com.ozcomingfroo.mybudget.MyBudgetIntents
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.data.preferences.AppLanguageMode
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyReminderNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun show(languageMode: AppLanguageMode) {
        if (!canPostNotifications()) return

        ensureChannel()
        val localizedContext = context.localizedFor(languageMode)
        val notification = NotificationCompat.Builder(context, ReminderChannelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(localizedContext.getString(R.string.daily_reminder_notification_title))
            .setContentText(localizedContext.getString(R.string.daily_reminder_notification_body))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(localizedContext.getString(R.string.daily_reminder_notification_body)),
            )
            .setContentIntent(addTransactionPendingIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(ReminderNotificationId, notification)
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            ReminderChannelId,
            context.getString(R.string.daily_reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.daily_reminder_channel_description)
        }
        manager.createNotificationChannel(channel)
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
            ReminderNotificationRequestCode,
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

    private companion object {
        const val ReminderChannelId = "daily_transaction_reminder"
        const val ReminderNotificationId = 3001
        const val ReminderNotificationRequestCode = 3002

        @Suppress("DEPRECATION")
        val HebrewLocale = Locale("iw")
    }
}
