package com.ozcomingfroo.mybudget.ui

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.core.money.MoneyFormatter
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import com.ozcomingfroo.mybudget.data.preferences.AppLanguageMode
import com.ozcomingfroo.mybudget.data.preferences.AppPreferences
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import com.ozcomingfroo.mybudget.data.preferences.AppThemeMode
import com.ozcomingfroo.mybudget.data.preferences.DefaultTransactionType
import com.ozcomingfroo.mybudget.data.repository.BudgetBookRepository
import com.ozcomingfroo.mybudget.data.repository.CategoryRepository
import com.ozcomingfroo.mybudget.data.repository.TransactionRepository
import com.ozcomingfroo.mybudget.ui.onboarding.OnboardingScreen
import com.ozcomingfroo.mybudget.ui.theme.BudgetBlack
import com.ozcomingfroo.mybudget.ui.theme.BudgetGreen
import com.ozcomingfroo.mybudget.ui.theme.BudgetGreenDark
import com.ozcomingfroo.mybudget.ui.theme.BudgetSurface
import com.ozcomingfroo.mybudget.ui.theme.BudgetWarmYellow
import com.ozcomingfroo.mybudget.ui.theme.ExpenseRed
import com.ozcomingfroo.mybudget.ui.theme.IncomeGreen
import com.ozcomingfroo.mybudget.ui.theme.MyBudgetTheme
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
internal fun HistoryScreen(
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    transactionRepository: TransactionRepository,
    clock: Clock,
    updateTransaction: suspend (TransactionEntity) -> Unit,
    onAddTransaction: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val defaultFilter = remember(clock) { HistoryFilter.currentMonth(clock) }
    var startEpochDay by rememberSaveable(defaultFilter.startDate) {
        mutableStateOf(defaultFilter.startDate.toEpochDay())
    }
    var endEpochDay by rememberSaveable(defaultFilter.endDate) {
        mutableStateOf(defaultFilter.endDate.toEpochDay())
    }
    var typeFilter by rememberSaveable { mutableStateOf(defaultFilter.type) }
    var selectedCategoryIds by rememberSaveable { mutableStateOf(emptyList<Long>()) }
    var showFiltersSheet by rememberSaveable { mutableStateOf(false) }
    var showCategoryFilterSheet by rememberSaveable { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    val categoryById = categories.associateBy { it.id }
    val activeFilter = HistoryFilter(
        startDate = LocalDate.ofEpochDay(startEpochDay),
        endDate = LocalDate.ofEpochDay(endEpochDay),
        type = typeFilter,
        selectedCategoryIds = selectedCategoryIds.toSet(),
    )
    val filteredTransactions = remember(transactions, activeFilter) {
        transactions.filterByHistoryFilter(activeFilter)
    }
    val groupedTransactions = filteredTransactions.groupBy { YearMonth.from(it.occurredAt.toLocalDate()) }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.transaction_deleted)
    val undoLabel = stringResource(R.string.undo)
    val isFilterActive = activeFilter != defaultFilter

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { onAddTransaction() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.add_transaction))
                }
                FilledTonalButton(
                    onClick = { showFiltersSheet = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFilterActive) {
                            stringResource(R.string.filters_active)
                        } else {
                            stringResource(R.string.filters)
                        },
                    )
                }
            }
        }
        if (transactions.isEmpty()) {
            item {
                EmptyState(
                    text = stringResource(R.string.no_transactions_yet),
                    actionLabel = stringResource(R.string.add_transaction),
                    onAction = { onAddTransaction() },
                )
            }
        } else if (filteredTransactions.isEmpty()) {
            item {
                EmptyState(
                    text = stringResource(R.string.no_transactions_match_filters),
                    actionLabel = stringResource(R.string.reset_filters),
                    onAction = {
                        startEpochDay = defaultFilter.startDate.toEpochDay()
                        endEpochDay = defaultFilter.endDate.toEpochDay()
                        typeFilter = defaultFilter.type
                        selectedCategoryIds = emptyList()
                    },
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
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilledTonalIconButton(onClick = { editingTransaction = transaction }) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = stringResource(R.string.edit_transaction),
                                    )
                                }
                                FilledTonalIconButton(
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
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.delete_transaction),
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    if (showFiltersSheet) {
        HistoryFiltersSheet(
            categories = categories,
            defaultFilter = defaultFilter,
            initialFilter = activeFilter,
            onApply = { filter ->
                startEpochDay = filter.startDate.toEpochDay()
                endEpochDay = filter.endDate.toEpochDay()
                typeFilter = filter.type
                selectedCategoryIds = filter.selectedCategoryIds.toList()
                showFiltersSheet = false
                showCategoryFilterSheet = false
            },
            onDismiss = {
                showFiltersSheet = false
                showCategoryFilterSheet = false
            },
            onOpenCategoryFilter = { showCategoryFilterSheet = true },
            showCategoryFilterSheet = showCategoryFilterSheet,
            onCategoryFilterDismiss = { showCategoryFilterSheet = false },
        )
    }

    editingTransaction?.let { transaction ->
        EditTransactionSheet(
            transaction = transaction,
            categories = categories,
            clock = clock,
            onDismiss = { editingTransaction = null },
            onTransactionUpdated = updateTransaction,
            snackbarHostState = snackbarHostState,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryFiltersSheet(
    categories: List<CategoryEntity>,
    defaultFilter: HistoryFilter,
    initialFilter: HistoryFilter,
    onApply: (HistoryFilter) -> Unit,
    onDismiss: () -> Unit,
    onOpenCategoryFilter: () -> Unit,
    showCategoryFilterSheet: Boolean,
    onCategoryFilterDismiss: () -> Unit,
) {
    var startDate by rememberSaveable(initialFilter) { mutableStateOf(initialFilter.startDate) }
    var endDate by rememberSaveable(initialFilter) { mutableStateOf(initialFilter.endDate) }
    var type by rememberSaveable(initialFilter) { mutableStateOf(initialFilter.type) }
    var selectedCategoryIds by rememberSaveable(initialFilter) {
        mutableStateOf(initialFilter.selectedCategoryIds.toList())
    }
    val availableCategories = remember(categories, type) {
        categories.availableForHistoryFilter(type)
    }
    LaunchedEffect(availableCategories) {
        val availableIds = availableCategories.map { it.id }.toSet()
        selectedCategoryIds = selectedCategoryIds.filter { it in availableIds }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.filters),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.date_range),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HistoryDateButton(
                    label = stringResource(R.string.start_date),
                    date = startDate,
                    modifier = Modifier.weight(1f),
                    onDateSelected = { selectedDate ->
                        startDate = selectedDate
                        if (endDate.isBefore(selectedDate)) {
                            endDate = selectedDate
                        }
                    },
                )
                HistoryDateButton(
                    label = stringResource(R.string.end_date),
                    date = endDate,
                    modifier = Modifier.weight(1f),
                    onDateSelected = { selectedDate ->
                        endDate = selectedDate
                        if (startDate.isAfter(selectedDate)) {
                            startDate = selectedDate
                        }
                    },
                )
            }
            Text(
                text = stringResource(R.string.transaction_type),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HistoryTransactionTypeFilter.entries.forEach { option ->
                    FilterChip(
                        selected = type == option,
                        onClick = { type = option },
                        label = { Text(option.label()) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            FilledTonalButton(
                onClick = onOpenCategoryFilter,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (selectedCategoryIds.isEmpty()) {
                        stringResource(R.string.all_categories)
                    } else {
                        stringResource(R.string.selected_categories_count, selectedCategoryIds.size)
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = {
                        startDate = defaultFilter.startDate
                        endDate = defaultFilter.endDate
                        type = defaultFilter.type
                        selectedCategoryIds = emptyList()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.reset_filters))
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        onApply(
                            HistoryFilter(
                                startDate = startDate,
                                endDate = endDate,
                                type = type,
                                selectedCategoryIds = selectedCategoryIds.toSet(),
                            ),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
    }

    if (showCategoryFilterSheet) {
        HistoryCategoryFilterSheet(
            categories = availableCategories,
            selectedCategoryIds = selectedCategoryIds.toSet(),
            onSelectionChanged = { selectedCategoryIds = it.toList() },
            onDismiss = onCategoryFilterDismiss,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryDateButton(
    label: String,
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    FilledTonalButton(
        onClick = { showPicker = true },
        modifier = modifier.heightIn(min = 56.dp),
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = date.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
    if (showPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.toPickerMillis())
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateSelected(it.toPickerDate()) }
                        showPicker = false
                    },
                ) {
                    Text(stringResource(R.string.apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryCategoryFilterSheet(
    categories: List<CategoryEntity>,
    selectedCategoryIds: Set<Long>,
    onSelectionChanged: (Set<Long>) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.categories),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (categories.isEmpty()) {
                EmptyState(text = stringResource(R.string.no_categories_yet))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 460.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    gridItems(categories, key = { it.id }) { category ->
                        val selected = category.id in selectedCategoryIds
                        CategoryGridTile(
                            category = category,
                            selected = selected,
                            onClick = {
                                onSelectionChanged(
                                    if (selected) {
                                        selectedCategoryIds - category.id
                                    } else {
                                        selectedCategoryIds + category.id
                                    },
                                )
                            },
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = { onSelectionChanged(emptySet()) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.all_categories))
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
    }
}

internal enum class HistoryTransactionTypeFilter {
    Expense,
    Income,
    Both,
}

internal data class HistoryFilter(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val type: HistoryTransactionTypeFilter,
    val selectedCategoryIds: Set<Long>,
) {
    companion object {
        fun currentMonth(clock: Clock): HistoryFilter {
            val month = YearMonth.now(clock)
            return HistoryFilter(
                startDate = month.atDay(1),
                endDate = month.atEndOfMonth(),
                type = HistoryTransactionTypeFilter.Both,
                selectedCategoryIds = emptySet(),
            )
        }
    }
}

internal fun List<TransactionEntity>.filterByHistoryFilter(filter: HistoryFilter): List<TransactionEntity> =
    filter { transaction ->
        val date = transaction.occurredAt.toLocalDate()
        val matchesDate = !date.isBefore(filter.startDate) && !date.isAfter(filter.endDate)
        val matchesType = when (filter.type) {
            HistoryTransactionTypeFilter.Expense -> transaction.type == TransactionType.EXPENSE
            HistoryTransactionTypeFilter.Income -> transaction.type == TransactionType.INCOME
            HistoryTransactionTypeFilter.Both -> true
        }
        val matchesCategory = filter.selectedCategoryIds.isEmpty() ||
            transaction.categoryId in filter.selectedCategoryIds
        matchesDate && matchesType && matchesCategory
    }

@Composable
private fun HistoryTransactionTypeFilter.label(): String = when (this) {
    HistoryTransactionTypeFilter.Expense -> stringResource(R.string.expense)
    HistoryTransactionTypeFilter.Income -> stringResource(R.string.income)
    HistoryTransactionTypeFilter.Both -> stringResource(R.string.both)
}

private fun List<CategoryEntity>.availableForHistoryFilter(type: HistoryTransactionTypeFilter): List<CategoryEntity> =
    filter { category ->
        category.archivedAt == null && when (type) {
            HistoryTransactionTypeFilter.Expense ->
                category.type == CategoryType.EXPENSE || category.type == CategoryType.BOTH
            HistoryTransactionTypeFilter.Income ->
                category.type == CategoryType.INCOME || category.type == CategoryType.BOTH
            HistoryTransactionTypeFilter.Both -> true
        }
    }.sortedWith(compareBy<CategoryEntity> { it.sortOrder }.thenBy { it.title })

private fun LocalDate.toPickerMillis(): Long =
    atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toPickerDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
