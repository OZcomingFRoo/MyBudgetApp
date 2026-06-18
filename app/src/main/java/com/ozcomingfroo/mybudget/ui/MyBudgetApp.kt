package com.ozcomingfroo.mybudget.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.entity.RecurringTransactionEntity
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.preferences.AppLanguageMode
import com.ozcomingfroo.mybudget.data.preferences.AppPreferences
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import com.ozcomingfroo.mybudget.data.preferences.AppThemeMode
import com.ozcomingfroo.mybudget.data.repository.BudgetBookRepository
import com.ozcomingfroo.mybudget.data.repository.CategoryRepository
import com.ozcomingfroo.mybudget.data.repository.RecurringTransactionRepository
import com.ozcomingfroo.mybudget.data.repository.TransactionRepository
import com.ozcomingfroo.mybudget.ui.onboarding.OnboardingScreen
import com.ozcomingfroo.mybudget.ui.theme.MyBudgetTheme
import java.time.Clock
import java.util.Locale
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
@Suppress("DEPRECATION")
private val HebrewLocale = Locale("iw")

@Composable
fun MyBudgetApp(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    recurringTransactionRepository: RecurringTransactionRepository,
    budgetBookRepository: BudgetBookRepository,
    appPreferencesRepository: AppPreferencesRepository,
    clock: Clock,
) {
    val preferences by remember(appPreferencesRepository) {
        appPreferencesRepository.preferences.map<AppPreferences, AppPreferences?> { it }
    }.collectAsState(initial = null)
    val loadedPreferences = preferences

    if (loadedPreferences == null) {
        LocalizedApp(languageMode = AppLanguageMode.HE) {
            MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                )
            }
        }
        return
    }

    LocalizedApp(languageMode = loadedPreferences.languageMode) {
        MyBudgetTheme(themeMode = loadedPreferences.themeMode) {
            val selectedBudgetBookId = loadedPreferences.selectedBudgetBookId
            val budgetBooks by remember {
                budgetBookRepository.observeActiveBudgetBooks()
            }.collectAsState(initial = emptyList())
            val currentBudgetBook = budgetBooks.firstOrNull { it.id == selectedBudgetBookId }
            val categories by remember(selectedBudgetBookId) {
                selectedBudgetBookId?.let(categoryRepository::observeActiveForBudgetBook) ?: flowOf(emptyList())
            }.collectAsState(initial = emptyList())
            val transactions by remember(selectedBudgetBookId) {
                selectedBudgetBookId?.let(transactionRepository::observeForBudgetBook) ?: flowOf(emptyList())
            }.collectAsState(initial = emptyList())
            val recurringTransactions by remember(selectedBudgetBookId) {
                selectedBudgetBookId?.let(recurringTransactionRepository::observeForBudgetBook) ?: flowOf(emptyList())
            }.collectAsState(initial = emptyList())

            if (loadedPreferences.hasCompletedOnboarding) {
                MyBudgetAppShell(
                    preferences = loadedPreferences,
                    selectedBudgetBookId = selectedBudgetBookId,
                    currentBudgetBook = currentBudgetBook,
                    categories = categories,
                    transactions = transactions,
                    recurringTransactions = recurringTransactions,
                    categoryRepository = categoryRepository,
                    transactionRepository = transactionRepository,
                    recurringTransactionRepository = recurringTransactionRepository,
                    appPreferencesRepository = appPreferencesRepository,
                    budgetBookRepository = budgetBookRepository,
                    clock = clock,
                )
            } else {
                OnboardingScreen(
                    preferences = loadedPreferences,
                    selectedBudgetBookId = selectedBudgetBookId,
                    appPreferencesRepository = appPreferencesRepository,
                    budgetBookRepository = budgetBookRepository,
                )
            }
        }
    }
}

@Composable
private fun LocalizedApp(
    languageMode: AppLanguageMode,
    content: @Composable () -> Unit,
) {
    val baseContext = LocalContext.current
    val systemConfiguration = LocalConfiguration.current
    val systemLayoutDirection = LocalLayoutDirection.current
    val localizedConfiguration = remember(systemConfiguration, languageMode) {
        when (languageMode) {
            AppLanguageMode.SYSTEM -> systemConfiguration
            AppLanguageMode.EN_US -> systemConfiguration.withLocale(Locale.US)
            AppLanguageMode.HE -> systemConfiguration.withLocale(HebrewLocale)
        }
    }
    val localizedContext = remember(baseContext, localizedConfiguration, languageMode) {
        if (languageMode == AppLanguageMode.SYSTEM) {
            baseContext
        } else {
            baseContext.createConfigurationContext(localizedConfiguration)
        }
    }
    val layoutDirection = when (languageMode) {
        AppLanguageMode.SYSTEM -> systemLayoutDirection
        AppLanguageMode.EN_US -> LayoutDirection.Ltr
        AppLanguageMode.HE -> LayoutDirection.Rtl
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
        LocalLayoutDirection provides layoutDirection,
        content = content,
    )
}

private enum class AppDestination(
    val route: String,
    val titleRes: Int,
) {
    Dashboard("dashboard", R.string.nav_dashboard),
    AddTransaction("add_transaction", R.string.nav_add_transaction),
    History("history", R.string.nav_history),
    Categories("categories", R.string.nav_categories),
    Reports("reports", R.string.nav_reports),
    RecurringTransactions("recurring_transactions", R.string.nav_recurring_transactions),
    Settings("settings", R.string.nav_settings),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyBudgetAppShell(
    preferences: AppPreferences,
    selectedBudgetBookId: Long?,
    currentBudgetBook: BudgetBookEntity?,
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    recurringTransactions: List<RecurringTransactionEntity>,
    categoryRepository: CategoryRepository,
    transactionRepository: TransactionRepository,
    recurringTransactionRepository: RecurringTransactionRepository,
    appPreferencesRepository: AppPreferencesRepository,
    budgetBookRepository: BudgetBookRepository,
    clock: Clock,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppDestination.Dashboard.route
    val currentDestination = AppDestination.entries.firstOrNull { it.route == currentRoute }
        ?: AppDestination.Dashboard

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(24.dp),
                )
                AppDestination.entries.forEach { destination ->
                    NavigationDrawerItem(
                        label = { Text(stringResource(destination.titleRes)) },
                        selected = currentRoute == destination.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(destination.route) {
                                popUpTo(AppDestination.Dashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(currentDestination.titleRes)) },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu_24),
                                contentDescription = stringResource(R.string.menu_button),
                            )
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppDestination.Dashboard.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                composable(AppDestination.Dashboard.route) {
                    DashboardScreen(
                        selectedBudgetBookId = selectedBudgetBookId,
                        categories = categories,
                        transactions = transactions,
                        clock = clock,
                        updateTransaction = transactionRepository::update,
                        snackbarHostState = snackbarHostState,
                        onAddTransaction = { navController.navigate(AppDestination.AddTransaction.route) },
                        onViewHistory = { navController.navigate(AppDestination.History.route) },
                        onOpenReports = { navController.navigate(AppDestination.Reports.route) },
                    )
                }
                composable(AppDestination.AddTransaction.route) {
                    AddTransactionScreen(
                        selectedBudgetBookId = selectedBudgetBookId,
                        categories = categories,
                        preferences = preferences,
                        insertTransaction = transactionRepository::insert,
                        clock = clock,
                        onTransactionSaved = {
                            navController.navigate(AppDestination.Dashboard.route) {
                                popUpTo(AppDestination.Dashboard.route)
                                launchSingleTop = true
                            }
                        },
                        snackbarHostState = snackbarHostState,
                    )
                }
                composable(AppDestination.History.route) {
                    HistoryScreen(
                        categories = categories,
                        transactions = transactions,
                        transactionRepository = transactionRepository,
                        clock = clock,
                        updateTransaction = transactionRepository::update,
                        onAddTransaction = { navController.navigate(AppDestination.AddTransaction.route) },
                        snackbarHostState = snackbarHostState,
                    )
                }
                composable(AppDestination.Categories.route) {
                    CategoriesScreen(
                        selectedBudgetBookId = selectedBudgetBookId,
                        categories = categories,
                        categoryRepository = categoryRepository,
                        snackbarHostState = snackbarHostState,
                    )
                }
                composable(AppDestination.Reports.route) {
                    ReportsScreen(
                        categories = categories,
                        transactions = transactions,
                        clock = clock,
                        onAddTransaction = { navController.navigate(AppDestination.AddTransaction.route) },
                    )
                }
                composable(AppDestination.RecurringTransactions.route) {
                    RecurringTransactionsScreen(
                        selectedBudgetBookId = selectedBudgetBookId,
                        categories = categories,
                        recurringTransactions = recurringTransactions,
                        recurringTransactionRepository = recurringTransactionRepository,
                        clock = clock,
                        snackbarHostState = snackbarHostState,
                    )
                }
                composable(AppDestination.Settings.route) {
                    SettingsScreen(
                        preferences = preferences,
                        currentBudgetBook = currentBudgetBook,
                        appPreferencesRepository = appPreferencesRepository,
                        budgetBookRepository = budgetBookRepository,
                    )
                }
            }
        }
    }
}

private fun Configuration.withLocale(locale: Locale): Configuration {
    val configuration = Configuration(this)
    configuration.setLocale(locale)
    return configuration
}
