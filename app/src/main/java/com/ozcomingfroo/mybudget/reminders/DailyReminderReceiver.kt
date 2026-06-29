package com.ozcomingfroo.mybudget.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DailyReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var appPreferencesRepository: AppPreferencesRepository

    @Inject
    lateinit var scheduler: DailyReminderScheduler

    @Inject
    lateinit var notifier: DailyReminderNotifier

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val preferences = appPreferencesRepository.getPreferences()
                when (intent.action) {
                    DailyReminderScheduler.ACTION_SHOW_DAILY_REMINDER -> {
                        if (preferences.dailyReminderEnabled) {
                            notifier.show(preferences.languageMode)
                        }
                        scheduler.schedule(preferences)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
