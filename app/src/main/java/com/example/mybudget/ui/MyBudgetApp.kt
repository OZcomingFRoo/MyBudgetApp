package com.example.mybudget.ui

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mybudget.R
import com.example.mybudget.core.money.MoneyFormatter
import com.example.mybudget.data.local.entity.CategoryEntity
import com.example.mybudget.data.local.entity.TransactionEntity
import com.example.mybudget.data.local.model.CategoryType
import com.example.mybudget.data.local.model.TransactionType
import com.example.mybudget.data.preferences.AppLanguageMode
import com.example.mybudget.data.preferences.AppPreferences
import com.example.mybudget.data.preferences.AppPreferencesRepository
import com.example.mybudget.data.preferences.AppThemeMode
import com.example.mybudget.data.preferences.DefaultTransactionType
import com.example.mybudget.data.repository.CategoryRepository
import com.example.mybudget.data.repository.TransactionRepository
import com.example.mybudget.ui.theme.ExpenseRed
import com.example.mybudget.ui.theme.IncomeGreen
import com.example.mybudget.ui.theme.MyBudgetTheme
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
private val HebrewLocale = Locale("iw")

@Composable
fun MyBudgetApp(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    appPreferencesRepository: AppPreferencesRepository,
    clock: Clock,
) {
    val preferences by appPreferencesRepository.preferences.collectAsState(
        initial = AppPreferences(
            selectedBudgetBookId = null,
            themeMode = AppThemeMode.SYSTEM,
            languageMode = AppLanguageMode.SYSTEM,
            hasCompletedOnboarding = false,
            defaultTransactionType = DefaultTransactionType.EXPENSE,
        ),
    )

    LocalizedApp(languageMode = preferences.languageMode) {
        MyBudgetTheme(themeMode = preferences.themeMode) {
            val selectedBudgetBookId = preferences.selectedBudgetBookId
            val categories by remember(selectedBudgetBookId) {
                selectedBudgetBookId?.let(categoryRepository::observeActiveForBudgetBook) ?: flowOf(emptyList())
            }.collectAsState(initial = emptyList())
            val transactions by remember(selectedBudgetBookId) {
                selectedBudgetBookId?.let(transactionRepository::observeForBudgetBook) ?: flowOf(emptyList())
            }.collectAsState(initial = emptyList())

            MyBudgetAppShell(
                preferences = preferences,
                selectedBudgetBookId = selectedBudgetBookId,
                categories = categories,
                transactions = transactions,
                transactionRepository = transactionRepository,
                appPreferencesRepository = appPreferencesRepository,
                clock = clock,
            )
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
    Settings("settings", R.string.nav_settings),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyBudgetAppShell(
    preferences: AppPreferences,
    selectedBudgetBookId: Long?,
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    transactionRepository: TransactionRepository,
    appPreferencesRepository: AppPreferencesRepository,
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
                        navController = navController,
                    )
                }
                composable(AppDestination.AddTransaction.route) {
                    AddTransactionScreen(
                        selectedBudgetBookId = selectedBudgetBookId,
                        categories = categories,
                        preferences = preferences,
                        transactionRepository = transactionRepository,
                        clock = clock,
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                    )
                }
                composable(AppDestination.History.route) {
                    HistoryScreen(
                        categories = categories,
                        transactions = transactions,
                        transactionRepository = transactionRepository,
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                    )
                }
                composable(AppDestination.Categories.route) {
                    CategoriesScreen(categories = categories)
                }
                composable(AppDestination.Reports.route) {
                    ReportsScreen(
                        categories = categories,
                        transactions = transactions,
                        clock = clock,
                    )
                }
                composable(AppDestination.Settings.route) {
                    SettingsScreen(
                        preferences = preferences,
                        appPreferencesRepository = appPreferencesRepository,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    selectedBudgetBookId: Long?,
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    clock: Clock,
    navController: NavHostController,
) {
    val month = YearMonth.now(clock)
    val monthlyTransactions = transactions.filter { YearMonth.from(it.occurredDate) == month }
    val income = monthlyTransactions.total(TransactionType.INCOME)
    val expenses = monthlyTransactions.total(TransactionType.EXPENSE)
    val remaining = income - expenses
    val categoryById = categories.associateBy { it.id }
    val locale = LocalContext.current.resources.configuration.locales[0]
    val monthName = month.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.dashboard_month_label, monthName),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            SummaryMetricCard(
                label = stringResource(R.string.remaining_this_month),
                amountMinor = remaining,
                supportingText = stringResource(R.string.income_minus_expenses),
                emphasized = true,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryMetricCard(
                    label = stringResource(R.string.income),
                    amountMinor = income,
                    modifier = Modifier.weight(1f),
                    amountColor = IncomeGreen,
                )
                SummaryMetricCard(
                    label = stringResource(R.string.expenses),
                    amountMinor = expenses,
                    modifier = Modifier.weight(1f),
                    amountColor = ExpenseRed,
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { navController.navigate(AppDestination.AddTransaction.route) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.add_expense))
                }
                FilledTonalButton(
                    onClick = { navController.navigate(AppDestination.AddTransaction.route) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.add_income))
                }
            }
        }
        item {
            SectionHeader(
                title = stringResource(R.string.recent_transactions),
                actionLabel = stringResource(R.string.view_all),
                onAction = { navController.navigate(AppDestination.History.route) },
            )
        }
        if (selectedBudgetBookId == null) {
            item { EmptyState(text = stringResource(R.string.loading_budget_book)) }
        } else if (transactions.isEmpty()) {
            item {
                EmptyState(
                    text = stringResource(R.string.no_transactions_yet),
                    actionLabel = stringResource(R.string.add_transaction),
                    onAction = { navController.navigate(AppDestination.AddTransaction.route) },
                )
            }
        } else {
            items(transactions.take(5), key = { it.id }) { transaction ->
                TransactionRow(
                    transaction = transaction,
                    category = transaction.categoryId?.let(categoryById::get),
                )
            }
        }
        item {
            SectionHeader(
                title = stringResource(R.string.report_preview),
                actionLabel = stringResource(R.string.open_reports),
                onAction = { navController.navigate(AppDestination.Reports.route) },
            )
            Text(
                text = stringResource(R.string.report_preview_body, MoneyFormatter.formatAmount(expenses), MoneyFormatter.formatAmount(income)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionScreen(
    selectedBudgetBookId: Long?,
    categories: List<CategoryEntity>,
    preferences: AppPreferences,
    transactionRepository: TransactionRepository,
    clock: Clock,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    var transactionType by rememberSaveable(preferences.defaultTransactionType) {
        mutableStateOf(
            when (preferences.defaultTransactionType) {
                DefaultTransactionType.EXPENSE -> TransactionType.EXPENSE
                DefaultTransactionType.INCOME -> TransactionType.INCOME
            },
        )
    }
    val availableCategories = categories.filter { category ->
        when (transactionType) {
            TransactionType.EXPENSE -> category.type == CategoryType.EXPENSE || category.type == CategoryType.BOTH
            TransactionType.INCOME -> category.type == CategoryType.INCOME || category.type == CategoryType.BOTH
        }
    }
    var selectedCategoryId by rememberSaveable(transactionType) {
        mutableStateOf(availableCategories.firstOrNull()?.id)
    }
    LaunchedEffect(availableCategories) {
        if (selectedCategoryId !in availableCategories.map { it.id }) {
            selectedCategoryId = availableCategories.firstOrNull()?.id
        }
    }

    var amountText by rememberSaveable { mutableStateOf("") }
    var titleText by rememberSaveable { mutableStateOf("") }
    var noteText by rememberSaveable { mutableStateOf("") }
    var dateText by rememberSaveable { mutableStateOf(LocalDate.now(clock).toString()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val savedMessage = stringResource(R.string.transaction_saved)
    val missingBookMessage = stringResource(R.string.loading_budget_book)
    val invalidAmountMessage = stringResource(R.string.amount_error)
    val invalidDateMessage = stringResource(R.string.date_error)
    val missingCategoryMessage = stringResource(R.string.category_error)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionType.entries.forEach { type ->
                    FilterChip(
                        selected = transactionType == type,
                        onClick = { transactionType = type },
                        label = { Text(type.label()) },
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                label = { Text(stringResource(R.string.amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
            ) {
                OutlinedTextField(
                    value = availableCategories.firstOrNull { it.id == selectedCategoryId }?.title.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                ) {
                    availableCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.title) },
                            onClick = {
                                selectedCategoryId = category.id
                                categoryExpanded = false
                            },
                        )
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = dateText,
                onValueChange = { dateText = it },
                label = { Text(stringResource(R.string.date)) },
                supportingText = { Text(stringResource(R.string.date_format_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text(stringResource(R.string.title_optional)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text(stringResource(R.string.note_optional)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        errorText?.let { error ->
            item {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            Button(
                onClick = {
                    val budgetBookId = selectedBudgetBookId
                    if (budgetBookId == null) {
                        errorText = missingBookMessage
                        return@Button
                    }
                    val amountMinor = try {
                        MoneyFormatter.parseAmountMinor(amountText)
                    } catch (_: IllegalArgumentException) {
                        errorText = invalidAmountMessage
                        return@Button
                    }
                    val date = try {
                        LocalDate.parse(dateText)
                    } catch (_: DateTimeParseException) {
                        errorText = invalidDateMessage
                        return@Button
                    }
                    val categoryId = selectedCategoryId
                    if (categoryId == null) {
                        errorText = missingCategoryMessage
                        return@Button
                    }
                    scope.launch {
                        val now = clock.instant()
                        transactionRepository.insert(
                            TransactionEntity(
                                budgetBookId = budgetBookId,
                                categoryId = categoryId,
                                type = transactionType,
                                amountMinor = amountMinor,
                                title = titleText.trim().ifBlank { null },
                                note = noteText.trim().ifBlank { null },
                                occurredDate = date,
                                createdAt = now,
                                updatedAt = now,
                            ),
                        )
                        snackbarHostState.showSnackbar(savedMessage)
                        navController.navigate(AppDestination.Dashboard.route) {
                            popUpTo(AppDestination.Dashboard.route)
                            launchSingleTop = true
                        }
                    }
                },
                enabled = amountText.isNotBlank() && selectedBudgetBookId != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save_transaction))
            }
        }
    }
}

@Composable
private fun HistoryScreen(
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    transactionRepository: TransactionRepository,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    val categoryById = categories.associateBy { it.id }
    val groupedTransactions = transactions.groupBy { YearMonth.from(it.occurredDate) }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.transaction_deleted)
    val undoLabel = stringResource(R.string.undo)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Button(
                onClick = { navController.navigate(AppDestination.AddTransaction.route) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.add_transaction))
            }
        }
        if (transactions.isEmpty()) {
            item {
                EmptyState(
                    text = stringResource(R.string.no_transactions_yet),
                    actionLabel = stringResource(R.string.add_transaction),
                    onAction = { navController.navigate(AppDestination.AddTransaction.route) },
                )
            }
        } else {
            groupedTransactions.forEach { (month, monthTransactions) ->
                item {
                    Text(
                        text = month.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(monthTransactions, key = { it.id }) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        category = transaction.categoryId?.let(categoryById::get),
                        trailing = {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        transactionRepository.delete(transaction)
                                        val result = snackbarHostState.showSnackbar(
                                            message = deletedMessage,
                                            actionLabel = undoLabel,
                                            duration = SnackbarDuration.Short,
                                        )
                                        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                            transactionRepository.insert(transaction.copy(id = 0))
                                        }
                                    }
                                },
                            ) {
                                Text(stringResource(R.string.delete))
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriesScreen(categories: List<CategoryEntity>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.categories_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        CategoryType.entries.forEach { type ->
            val sectionCategories = categories.filter { it.type == type }
            if (sectionCategories.isNotEmpty()) {
                item {
                    Text(
                        text = type.label(),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
                items(sectionCategories, key = { it.id }) { category ->
                    CategoryRow(category = category)
                }
            }
        }
        if (categories.isEmpty()) {
            item { EmptyState(text = stringResource(R.string.no_categories_yet)) }
        }
    }
}

@Composable
private fun ReportsScreen(
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    clock: Clock,
) {
    val month = YearMonth.now(clock)
    val monthlyTransactions = transactions.filter { YearMonth.from(it.occurredDate) == month }
    val income = monthlyTransactions.total(TransactionType.INCOME)
    val expenses = monthlyTransactions.total(TransactionType.EXPENSE)
    val categoryById = categories.associateBy { it.id }
    val spendingByCategory = monthlyTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.categoryId }
        .mapValues { (_, items) -> items.sumOf { it.amountMinor } }
        .toList()
        .sortedByDescending { it.second }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.current_month),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryMetricCard(
                    label = stringResource(R.string.income),
                    amountMinor = income,
                    modifier = Modifier.weight(1f),
                    amountColor = IncomeGreen,
                )
                SummaryMetricCard(
                    label = stringResource(R.string.expenses),
                    amountMinor = expenses,
                    modifier = Modifier.weight(1f),
                    amountColor = ExpenseRed,
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.spending_by_category),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (spendingByCategory.isEmpty()) {
            item { EmptyState(text = stringResource(R.string.no_report_data)) }
        } else {
            items(spendingByCategory, key = { it.first ?: -1L }) { (categoryId, amountMinor) ->
                val category = categoryId?.let(categoryById::get)
                CategoryAmountRow(
                    title = category?.title ?: stringResource(R.string.uncategorized),
                    amountMinor = amountMinor,
                    color = category?.toColor() ?: MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    preferences: AppPreferences,
    appPreferencesRepository: AppPreferencesRepository,
) {
    val scope = rememberCoroutineScope()
    val versionName = rememberAppVersionName() ?: stringResource(R.string.version_unavailable)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SettingsSection(title = stringResource(R.string.settings_appearance)) {
                AppThemeMode.entries.forEach { mode ->
                    RadioSettingRow(
                        label = mode.label(),
                        selected = preferences.themeMode == mode,
                        onClick = { scope.launch { appPreferencesRepository.setThemeMode(mode) } },
                    )
                }
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_language)) {
                AppLanguageMode.entries.forEach { mode ->
                    RadioSettingRow(
                        label = mode.label(),
                        selected = preferences.languageMode == mode,
                        onClick = { scope.launch { appPreferencesRepository.setLanguageMode(mode) } },
                    )
                }
            }
        }
        item {
            SettingsSection(
                title = stringResource(R.string.settings_transaction_defaults),
                supportingText = stringResource(R.string.default_transaction_type_helper),
            ) {
                DefaultTransactionType.entries.forEach { type ->
                    RadioSettingRow(
                        label = type.label(),
                        selected = preferences.defaultTransactionType == type,
                        onClick = { scope.launch { appPreferencesRepository.setDefaultTransactionType(type) } },
                    )
                }
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_data_privacy)) {
                SettingsInfoText(text = stringResource(R.string.data_privacy_local_only))
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_about)) {
                SettingsInfoText(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                SettingsInfoText(text = stringResource(R.string.app_version, versionName))
                SettingsInfoText(text = stringResource(R.string.about_app_purpose))
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    label: String,
    amountMinor: Long,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    amountColor: Color = MaterialTheme.colorScheme.onSurface,
    emphasized: Boolean = false,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (emphasized) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = MoneyFormatter.formatAmount(amountMinor),
                style = if (emphasized) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = amountColor,
            )
            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    trailing: (@Composable () -> Unit)? = null,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CategoryDot(color = category?.toColor() ?: MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title ?: category?.title ?: stringResource(R.string.uncategorized),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = listOfNotNull(category?.title, transaction.occurredDate.toString()).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = transaction.signedAmountText(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                    textAlign = TextAlign.End,
                )
                trailing?.invoke()
            }
        }
    }
}

@Composable
private fun CategoryRow(category: CategoryEntity) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CategoryDot(color = category.toColor())
            Column(modifier = Modifier.weight(1f)) {
                Text(category.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = category.iconName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = {},
                label = { Text(category.type.label()) },
            )
        }
    }
}

@Composable
private fun CategoryAmountRow(
    title: String,
    amountMinor: Long,
    color: Color,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CategoryDot(color = color)
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = MoneyFormatter.formatAmount(amountMinor),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CategoryDot(color: Color) {
    Box(
        modifier = Modifier
            .width(14.dp)
            .height(14.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onAction) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun EmptyState(
    text: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (supportingText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun SettingsInfoText(
    text: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        text = text,
        style = style,
        color = color,
        modifier = Modifier.padding(top = 12.dp),
    )
}

@Composable
private fun RadioSettingRow(
    label: String,
    supportingText: String? = null,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun rememberAppVersionName(): String? {
    val context = LocalContext.current
    return remember(context) { context.packageVersionName() }
}

private fun Configuration.withLocale(locale: Locale): Configuration {
    val configuration = Configuration(this)
    configuration.setLocale(locale)
    return configuration
}

private fun Context.packageVersionName(): String? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0).versionName
    }
}.getOrNull()?.takeIf { it.isNotBlank() }

private fun List<TransactionEntity>.total(type: TransactionType): Long =
    filter { it.type == type }.sumOf { it.amountMinor }

private fun TransactionEntity.signedAmountText(): String {
    val sign = if (type == TransactionType.INCOME) "+" else "-"
    return sign + MoneyFormatter.formatAmount(amountMinor)
}

private fun CategoryEntity.toColor(): Color =
    runCatching { Color(android.graphics.Color.parseColor(color)) }
        .getOrDefault(Color.Gray)

@Composable
private fun TransactionType.label(): String = when (this) {
    TransactionType.EXPENSE -> stringResource(R.string.expense)
    TransactionType.INCOME -> stringResource(R.string.income)
}

@Composable
private fun CategoryType.label(): String = when (this) {
    CategoryType.EXPENSE -> stringResource(R.string.expense)
    CategoryType.INCOME -> stringResource(R.string.income)
    CategoryType.BOTH -> stringResource(R.string.both)
}

@Composable
private fun AppThemeMode.label(): String = when (this) {
    AppThemeMode.DEFAULT -> stringResource(R.string.theme_default)
    AppThemeMode.NIGHT -> stringResource(R.string.theme_night)
    AppThemeMode.SYSTEM -> stringResource(R.string.theme_system)
}

@Composable
private fun AppLanguageMode.label(): String = when (this) {
    AppLanguageMode.SYSTEM -> stringResource(R.string.language_system)
    AppLanguageMode.EN_US -> stringResource(R.string.language_english_us)
    AppLanguageMode.HE -> stringResource(R.string.language_hebrew)
}

@Composable
private fun DefaultTransactionType.label(): String = when (this) {
    DefaultTransactionType.EXPENSE -> stringResource(R.string.expense)
    DefaultTransactionType.INCOME -> stringResource(R.string.income)
}
