package com.ozcomingfroo.mybudget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.ozcomingfroo.mybudget.data.LocalDataInitializer
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import com.ozcomingfroo.mybudget.data.repository.BudgetBookRepository
import com.ozcomingfroo.mybudget.data.repository.CategoryRepository
import com.ozcomingfroo.mybudget.data.repository.RecurringTransactionRepository
import com.ozcomingfroo.mybudget.data.repository.TransactionRepository
import com.ozcomingfroo.mybudget.ui.AppLaunchDestination
import com.ozcomingfroo.mybudget.ui.MyBudgetApp
import dagger.hilt.android.AndroidEntryPoint
import java.time.Clock
import javax.inject.Inject
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
    lateinit var clock: Clock

    private var launchDestination by mutableStateOf<AppLaunchDestination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchDestination = intent.toLaunchDestination()
        enableEdgeToEdge()
        lifecycleScope.launch {
            localDataInitializer.initialize()
        }
        setContent {
            MyBudgetApp(
                transactionRepository = transactionRepository,
                categoryRepository = categoryRepository,
                recurringTransactionRepository = recurringTransactionRepository,
                budgetBookRepository = budgetBookRepository,
                appPreferencesRepository = appPreferencesRepository,
                clock = clock,
                launchDestination = launchDestination,
                onLaunchDestinationHandled = { launchDestination = null },
            )
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
}
