package com.ozcomingfroo.mybudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.ozcomingfroo.mybudget.data.LocalDataInitializer
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import com.ozcomingfroo.mybudget.data.repository.BudgetBookRepository
import com.ozcomingfroo.mybudget.data.repository.CategoryRepository
import com.ozcomingfroo.mybudget.data.repository.RecurringTransactionRepository
import com.ozcomingfroo.mybudget.data.repository.TransactionRepository
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            )
        }
    }
}
