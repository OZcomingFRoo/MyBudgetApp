package com.ozcomingfroo.mybudget

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ozcomingfroo.mybudget.data.LocalDataInitializer
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import com.ozcomingfroo.mybudget.data.repository.BudgetBookRepository
import com.ozcomingfroo.mybudget.data.repository.CategoryRepository
import com.ozcomingfroo.mybudget.data.repository.RecurringTransactionRepository
import com.ozcomingfroo.mybudget.data.repository.TransactionRepository
import com.ozcomingfroo.mybudget.reminders.DailyReminderScheduler
import com.ozcomingfroo.mybudget.ui.AppLaunchDestination
import com.ozcomingfroo.mybudget.ui.MyBudgetApp
import dagger.hilt.android.AndroidEntryPoint
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var localDataInitializer: LocalDataInitializer

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var recurringTransactionRepository: RecurringTransactionRepository

    @Inject
    lateinit var budgetBookRepository: BudgetBookRepository

    @Inject
    lateinit var appPreferencesRepository: AppPreferencesRepository

    @Inject
    lateinit var dailyReminderScheduler: DailyReminderScheduler

    @Inject
    lateinit var clock: Clock

    private var launchDestination by mutableStateOf<AppLaunchDestination?>(null)
    private var hasRequestedNotificationPermission = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchDestination = intent.toLaunchDestination()
        enableEdgeToEdge()
        setContent {
            MyBudgetApp(
                transactionRepository = transactionRepository,
                categoryRepository = categoryRepository,
                recurringTransactionRepository = recurringTransactionRepository,
                budgetBookRepository = budgetBookRepository,
                appPreferencesRepository = appPreferencesRepository,
                dailyReminderScheduler = dailyReminderScheduler,
                clock = clock,
                launchDestination = launchDestination,
                onLaunchDestinationHandled = { launchDestination = null },
            )
        }
        window.decorView.post {
            lifecycleScope.launch {
                localDataInitializer.initialize()
            }
        }
        lifecycleScope.launch {
            appPreferencesRepository.preferences
                .map { it.dailyReminderEnabled }
                .distinctUntilChanged()
                .collect { enabled ->
                    requestNotificationPermissionIfNeeded(enabled)
                }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchDestination = intent.toLaunchDestination()
    }

    private fun Intent?.toLaunchDestination(): AppLaunchDestination? =
        when (this?.action) {
            MyBudgetIntents.ACTION_OPEN_DASHBOARD -> AppLaunchDestination.Dashboard
            MyBudgetIntents.ACTION_OPEN_ADD_TRANSACTION -> AppLaunchDestination.AddTransaction
            else -> null
        }

    private fun requestNotificationPermissionIfNeeded(enabled: Boolean) {
        if (!enabled ||
            hasRequestedNotificationPermission ||
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        hasRequestedNotificationPermission = true
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
