package com.ozcomingfroo.mybudget.ui

import android.content.Context
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import com.ozcomingfroo.mybudget.data.preferences.AppLanguageMode
import com.ozcomingfroo.mybudget.data.preferences.AppPreferences
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import com.ozcomingfroo.mybudget.data.preferences.AppThemeMode
import com.ozcomingfroo.mybudget.data.repository.BudgetBookRepository
import com.ozcomingfroo.mybudget.data.repository.CategoryRepository
import com.ozcomingfroo.mybudget.data.repository.RecurringTransactionRepository
import com.ozcomingfroo.mybudget.data.repository.TransactionRepository
import com.ozcomingfroo.mybudget.reminders.DailyReminderScheduler
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
    dailyReminderScheduler: DailyReminderScheduler,
    clock: Clock,
    launchDestination: AppLaunchDestination? = null,
    onLaunchDestinationHandled: () -> Unit = {},
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

            if (loadedPreferences.hasCompletedOnboarding) {
                MyBudgetAppShell(
                    preferences = loadedPreferences,
                    selectedBudgetBookId = selectedBudgetBookId,
                    currentBudgetBook = currentBudgetBook,
                    budgetBooks = budgetBooks,
                    categories = categories,
                    categoryRepository = categoryRepository,
                    transactionRepository = transactionRepository,
                    recurringTransactionRepository = recurringTransactionRepository,
                    appPreferencesRepository = appPreferencesRepository,
                    budgetBookRepository = budgetBookRepository,
                    dailyReminderScheduler = dailyReminderScheduler,
                    clock = clock,
                    launchDestination = launchDestination,
                    onLaunchDestinationHandled = onLaunchDestinationHandled,
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
    val icon: ImageVector,
    val showInDrawer: Boolean = true,
) {
    Dashboard("dashboard", R.string.nav_dashboard, Icons.Filled.Dashboard),
    AddTransaction("add_transaction", R.string.nav_add_transaction, Icons.Filled.AddCircle),
    History("history", R.string.nav_history, Icons.Filled.History),
    Categories("categories", R.string.nav_categories, Icons.Filled.Category),
    Reports("reports", R.string.nav_reports, Icons.Filled.BarChart),
    RecurringTransactions("recurring_transactions", R.string.nav_recurring_transactions, Icons.Filled.Repeat),
    Accounts("accounts", R.string.nav_accounts, Icons.Filled.Groups),
    CreateAccount("create_account", R.string.create_account, Icons.Filled.AccountBalance, showInDrawer = false),
    Settings("settings", R.string.nav_settings, Icons.Filled.Settings),
}

private const val AddTransactionTypeArg = "type"
private const val AddTransactionOriginArg = "origin"
private const val AddTransactionOriginDashboard = "dashboard"
private const val AddTransactionRoutePattern =
    "add_transaction?type={$AddTransactionTypeArg}&origin={$AddTransactionOriginArg}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyBudgetAppShell(
    preferences: AppPreferences,
    selectedBudgetBookId: Long?,
    currentBudgetBook: BudgetBookEntity?,
    budgetBooks: List<BudgetBookEntity>,
    categories: List<CategoryEntity>,
    categoryRepository: CategoryRepository,
    transactionRepository: TransactionRepository,
    recurringTransactionRepository: RecurringTransactionRepository,
    appPreferencesRepository: AppPreferencesRepository,
    budgetBookRepository: BudgetBookRepository,
    dailyReminderScheduler: DailyReminderScheduler,
    clock: Clock,
    launchDestination: AppLaunchDestination?,
    onLaunchDestinationHandled: () -> Unit,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppDestination.Dashboard.route
    val currentRouteBase = currentRoute.substringBefore("?")
    val currentDestination = AppDestination.entries.firstOrNull { it.route == currentRouteBase }
        ?: AppDestination.Dashboard
    val isDrawerActive = drawerState.currentValue != DrawerValue.Closed ||
        drawerState.targetValue != DrawerValue.Closed

    LaunchedEffect(
        preferences.dailyReminderEnabled,
        preferences.dailyReminderHour,
        preferences.dailyReminderMinute,
    ) {
        dailyReminderScheduler.schedule(preferences)
    }

    LaunchedEffect(launchDestination) {
        when (launchDestination) {
            AppLaunchDestination.Dashboard -> {
                navController.navigate(AppDestination.Dashboard.route) {
                    popUpTo(AppDestination.Dashboard.route)
                    launchSingleTop = true
                }
                onLaunchDestinationHandled()
            }
            AppLaunchDestination.AddTransaction -> {
                navController.navigate(AppDestination.AddTransaction.route) {
                    launchSingleTop = true
                }
                onLaunchDestinationHandled()
            }
            null -> Unit
        }
    }

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
                AppDestination.entries.filter { it.showInDrawer }.forEach { destination ->
                    NavigationDrawerItem(
                        label = { Text(stringResource(destination.titleRes)) },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null,
                            )
                        },
                        selected = currentRouteBase == destination.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(destination.route) {
                                popUpTo(AppDestination.Dashboard.route) {
                                    saveState = destination != AppDestination.Dashboard
                                }
                                launchSingleTop = true
                                restoreState = destination != AppDestination.Dashboard
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
                    title = {
                        Column {
                            Text(
                                text = stringResource(currentDestination.titleRes),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            currentBudgetBook?.title?.takeIf { it.isNotBlank() }?.let { accountTitle ->
                                Text(
                                    text = accountTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    },
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
                        transactionRepository = transactionRepository,
                        clock = clock,
                        updateTransaction = transactionRepository::update,
                        snackbarHostState = snackbarHostState,
                        onAddExpense = {
                            navController.navigateDashboardAddTransaction(TransactionType.EXPENSE)
                        },
                        onAddIncome = {
                            navController.navigateDashboardAddTransaction(TransactionType.INCOME)
                        },
                        onAddTransaction = { navController.navigate(AppDestination.AddTransaction.route) },
                        onViewHistory = { navController.navigate(AppDestination.History.route) },
                        onOpenReports = { navController.navigate(AppDestination.Reports.route) },
                    )
                }
                composable(
                    route = AddTransactionRoutePattern,
                    arguments = listOf(
                        navArgument(AddTransactionTypeArg) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        navArgument(AddTransactionOriginArg) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                    ),
                ) { addTransactionEntry ->
                    val initialTransactionType = addTransactionEntry.arguments
                        ?.getString(AddTransactionTypeArg)
                        ?.let(::parseTransactionTypeRouteArg)
                    val showCancelButton = addTransactionEntry.arguments
                        ?.getString(AddTransactionOriginArg) == AddTransactionOriginDashboard
                    AddTransactionScreen(
                        selectedBudgetBookId = selectedBudgetBookId,
                        categories = categories,
                        preferences = preferences,
                        insertTransaction = transactionRepository::insert,
                        clock = clock,
                        initialTransactionType = initialTransactionType,
                        showCancelButton = showCancelButton,
                        onCancel = {
                            navController.navigate(AppDestination.Dashboard.route) {
                                popUpTo(AppDestination.Dashboard.route)
                                launchSingleTop = true
                            }
                        },
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
                        selectedBudgetBookId = selectedBudgetBookId,
                        categories = categories,
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
                        selectedBudgetBookId = selectedBudgetBookId,
                        categories = categories,
                        transactionRepository = transactionRepository,
                        clock = clock,
                        onAddTransaction = { navController.navigate(AppDestination.AddTransaction.route) },
                    )
                }
                composable(AppDestination.RecurringTransactions.route) {
                    RecurringTransactionsScreen(
                        selectedBudgetBookId = selectedBudgetBookId,
                        categories = categories,
                        recurringTransactionRepository = recurringTransactionRepository,
                        clock = clock,
                        snackbarHostState = snackbarHostState,
                    )
                }
                composable(AppDestination.Accounts.route) {
                    AccountsScreen(
                        budgetBooks = budgetBooks,
                        selectedBudgetBookId = selectedBudgetBookId,
                        languageMode = preferences.languageMode,
                        appPreferencesRepository = appPreferencesRepository,
                        budgetBookRepository = budgetBookRepository,
                        snackbarHostState = snackbarHostState,
                        onCreateAccount = { navController.navigate(AppDestination.CreateAccount.route) },
                    )
                }
                composable(AppDestination.CreateAccount.route) {
                    CreateAccountScreen(
                        initialSeedLanguageMode = preferences.languageMode,
                        createBudgetBook = { title, description, starterCategoryTitles ->
                            budgetBookRepository.createBudgetBook(
                                title = title,
                                description = description,
                                selectAfterCreate = true,
                                starterCategoryTitles = starterCategoryTitles,
                            )
                        },
                        snackbarHostState = snackbarHostState,
                        onCreated = {
                            navController.navigate(AppDestination.Accounts.route) {
                                popUpTo(AppDestination.Accounts.route)
                                launchSingleTop = true
                            }
                        },
                        onCancel = {
                            navController.navigate(AppDestination.Accounts.route) {
                                popUpTo(AppDestination.Accounts.route)
                                launchSingleTop = true
                            }
                        },
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

            BackHandler(enabled = isDrawerActive || currentRouteBase != AppDestination.Dashboard.route) {
                if (isDrawerActive) {
                    scope.launch { drawerState.close() }
                } else {
                    navController.navigate(AppDestination.Dashboard.route) {
                        popUpTo(AppDestination.Dashboard.route)
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}

private fun androidx.navigation.NavHostController.navigateDashboardAddTransaction(type: TransactionType) {
    navigate(
        "${AppDestination.AddTransaction.route}?$AddTransactionTypeArg=${type.name}" +
            "&$AddTransactionOriginArg=$AddTransactionOriginDashboard",
    )
}

private fun parseTransactionTypeRouteArg(value: String): TransactionType? =
    TransactionType.entries.firstOrNull { it.name == value }

private fun Configuration.withLocale(locale: Locale): Configuration {
    val configuration = Configuration(this)
    configuration.setLocale(locale)
    return configuration
}
