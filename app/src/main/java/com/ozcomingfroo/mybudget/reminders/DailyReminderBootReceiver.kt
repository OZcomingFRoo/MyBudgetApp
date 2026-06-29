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
class DailyReminderBootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var appPreferencesRepository: AppPreferencesRepository

    @Inject
    lateinit var scheduler: DailyReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                scheduler.schedule(appPreferencesRepository.getPreferences())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
