package com.ozcomingfroo.mybudget.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.core.money.MoneyFormatter
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.entity.RecurringTransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import com.ozcomingfroo.mybudget.data.local.model.RecurringFrequency
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import com.ozcomingfroo.mybudget.data.repository.RecurringTransactionRepository
import com.ozcomingfroo.mybudget.domain.recurring.RecurringSchedule
import com.ozcomingfroo.mybudget.ui.theme.ExpenseRed
import com.ozcomingfroo.mybudget.ui.theme.IncomeGreen
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
internal fun RecurringTransactionsScreen(
    selectedBudgetBookId: Long?,
    categories: List<CategoryEntity>,
    recurringTransactionRepository: RecurringTransactionRepository,
    clock: Clock,
    snackbarHostState: SnackbarHostState,
) {
    val defaultFilter = RecurringTransactionsFilter()
    var typeFilter by rememberSaveable { mutableStateOf(defaultFilter.type) }
    var selectedCategoryIds by rememberSaveable { mutableStateOf(emptyList<Long>()) }
    var selectedFrequencies by rememberSaveable { mutableStateOf(emptyList<RecurringFrequency>()) }
    var statusFilter by rememberSaveable { mutableStateOf(defaultFilter.status) }
    var nextRunStartEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var nextRunEndEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var showFiltersSheet by rememberSaveable { mutableStateOf(false) }
    var showCategoryFilterSheet by rememberSaveable { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<RecurringTransactionEntity?>(null) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var deletingRule by remember { mutableStateOf<RecurringTransactionEntity?>(null) }
    val scope = rememberCoroutineScope()
    val recurringTransactions by remember(selectedBudgetBookId, recurringTransactionRepository) {
        selectedBudgetBookId?.let(recurringTransactionRepository::observeForBudgetBook) ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())
    val categoryById = remember(categories) { categories.associateBy { it.id } }
    val selectedCategoryIdSet = remember(selectedCategoryIds) { selectedCategoryIds.toSet() }
    val selectedFrequencySet = remember(selectedFrequencies) { selectedFrequencies.toSet() }
    val activeFilter = RecurringTransactionsFilter(
        type = typeFilter,
        selectedCategoryIds = selectedCategoryIdSet,
        selectedFrequencies = selectedFrequencySet,
        status = statusFilter,
        nextRunStartDate = nextRunStartEpochDay?.let(LocalDate::ofEpochDay),
        nextRunEndDate = nextRunEndEpochDay?.let(LocalDate::ofEpochDay),
    )
    val filteredRules = remember(recurringTransactions, activeFilter) {
        recurringTransactions.filterByRecurringTransactionsFilter(activeFilter)
    }
    val isFilterActive = activeFilter != defaultFilter
    val deletedMessage = stringResource(R.string.recurring_transaction_deleted)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = {
                        editingRule = null
                        showEditor = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("recurring_add"),
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_new))
                }
                FilledTonalButton(
                    onClick = { showFiltersSheet = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("recurring_filters"),
                ) {
                    Icon(imageVector = Icons.Filled.FilterList, contentDescription = null)
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
        item {
            RecurringFilterSummary(
                filter = activeFilter,
                categoryById = categoryById,
            )
        }
        if (recurringTransactions.isEmpty()) {
            item {
                EmptyState(
                    text = stringResource(R.string.no_recurring_transactions_yet),
                    actionLabel = stringResource(R.string.add_recurring_transaction),
                    onAction = {
                        editingRule = null
                        showEditor = true
                    },
                )
            }
        } else if (filteredRules.isEmpty()) {
            item {
                EmptyState(
                    text = stringResource(R.string.no_recurring_transactions_match_filters),
                    actionLabel = stringResource(R.string.reset_filters),
                    onAction = {
                        typeFilter = defaultFilter.type
                        selectedCategoryIds = emptyList()
                        selectedFrequencies = emptyList()
                        statusFilter = defaultFilter.status
                        nextRunStartEpochDay = null
                        nextRunEndEpochDay = null
                    },
                )
            }
        } else {
            items(
                items = filteredRules,
                key = { it.id },
                contentType = { "recurring_rule" },
            ) { rule ->
                RecurringTransactionRow(
                    rule = rule,
                    category = rule.categoryId?.let(categoryById::get),
                    onEdit = {
                        editingRule = rule
                        showEditor = true
                    },
                    onDelete = { deletingRule = rule },
                )
            }
        }
    }

    if (showFiltersSheet) {
        RecurringFiltersSheet(
            categories = categories,
            defaultFilter = defaultFilter,
            initialFilter = activeFilter,
            onApply = { filter ->
                typeFilter = filter.type
                selectedCategoryIds = filter.selectedCategoryIds.toList()
                selectedFrequencies = filter.selectedFrequencies.toList()
                statusFilter = filter.status
                nextRunStartEpochDay = filter.nextRunStartDate?.toEpochDay()
                nextRunEndEpochDay = filter.nextRunEndDate?.toEpochDay()
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

    if (showEditor) {
        RecurringTransactionEditorSheet(
            rule = editingRule,
            selectedBudgetBookId = selectedBudgetBookId,
            categories = categories,
            clock = clock,
            onDismiss = { showEditor = false },
            onSave = { rule ->
                if (rule.id == 0L) {
                    recurringTransactionRepository.insert(rule)
                } else {
                    recurringTransactionRepository.update(rule)
                }
            },
            snackbarHostState = snackbarHostState,
        )
    }

    deletingRule?.let { rule ->
        AlertDialog(
            onDismissRequest = { deletingRule = null },
            title = { Text(stringResource(R.string.delete_recurring_transaction_title)) },
            text = { Text(stringResource(R.string.delete_recurring_transaction_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            recurringTransactionRepository.delete(rule)
                            deletingRule = null
                            snackbarHostState.showSnackbar(deletedMessage)
                        }
                    },
                    modifier = Modifier.testTag("recurring_delete_confirm"),
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingRule = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun RecurringTransactionRow(
    rule: RecurringTransactionEntity,
    category: CategoryEntity?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val title = recurringTitle(
        rule = rule,
        categoryTitle = category?.title,
        uncategorized = stringResource(R.string.uncategorized),
    )
    val amountColor = if (rule.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
    val amountPrefix = if (rule.type == TransactionType.INCOME) "+" else "-"
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CategoryIconCircle(
                iconName = category?.iconName ?: "category",
                color = category?.toColor() ?: MaterialTheme.colorScheme.primary,
                contentDescription = category?.let {
                    stringResource(R.string.category_icon_content_description, it.title)
                },
                size = 42.dp,
                iconSize = 24.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = recurringSubtitle(rule),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = amountPrefix + MoneyFormatter.formatAmount(rule.amountMinor),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalIconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("recurring_edit_${rule.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_recurring_transaction),
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("recurring_delete_${rule.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete_recurring_transaction),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun recurringTitle(
    rule: RecurringTransactionEntity,
    categoryTitle: String?,
    uncategorized: String,
): String {
    val base = recurringBaseTitle(rule, categoryTitle, uncategorized)
    val phrase = rule.frequency.recurrencePhrase(rule.interval)
    val end = rule.endDate?.let { stringResource(R.string.recurring_until_date, it.toString()) }
    return listOfNotNull(base, phrase, end).joinToString(" ")
}

@Composable
private fun recurringSubtitle(rule: RecurringTransactionEntity): String {
    val status = if (rule.isActive) {
        stringResource(R.string.active)
    } else {
        stringResource(R.string.inactive)
    }
    return listOf(
        rule.type.label(),
        stringResource(R.string.recurring_next_run, rule.nextRunDate.toString()),
        status,
    ).joinToString(stringResource(R.string.transaction_subtitle_separator))
}

internal fun recurringBaseTitle(
    rule: RecurringTransactionEntity,
    categoryTitle: String?,
    uncategorized: String,
): String = rule.title?.takeIf { it.isNotBlank() } ?: categoryTitle ?: uncategorized

@Composable
private fun RecurringFrequency.recurrencePhrase(interval: Int): String =
    when (this) {
    RecurringFrequency.DAILY -> if (interval == 1) {
        stringResource(R.string.recurring_every_day)
    } else if (interval == 2) {
        stringResource(R.string.recurring_every_2_days)
    } else if (interval == 3) {
        stringResource(R.string.recurring_every_3_days)
    } else {
        stringResource(R.string.recurring_every_n_days, interval)
    }
    RecurringFrequency.WEEKLY -> if (interval == 1) {
        stringResource(R.string.recurring_every_week)
    } else if (interval == 2) {
        stringResource(R.string.recurring_every_2_weeks)
    } else if (interval == 3) {
        stringResource(R.string.recurring_every_3_weeks)
    } else {
        stringResource(R.string.recurring_every_n_weeks, interval)
    }
    RecurringFrequency.MONTHLY -> if (interval == 1) {
        stringResource(R.string.recurring_every_month)
    } else if (interval == 2) {
        stringResource(R.string.recurring_every_2_months)
    } else if (interval == 3) {
        stringResource(R.string.recurring_every_3_months)
    } else {
        stringResource(R.string.recurring_every_n_months, interval)
    }
        RecurringFrequency.YEARLY -> if (interval == 1) {
            stringResource(R.string.recurring_every_year)
        } else {
            stringResource(R.string.recurring_every_n_years, interval)
        }
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecurringFilterSummary(
    filter: RecurringTransactionsFilter,
    categoryById: Map<Long, CategoryEntity>,
) {
    val selectedCategories = filter.selectedCategoryIds
        .map { categoryId -> categoryId to categoryById[categoryId] }
        .sortedWith(
            compareBy<Pair<Long, CategoryEntity?>> { it.second?.sortOrder ?: Int.MAX_VALUE }
                .thenBy { it.second?.title ?: "" },
        )

    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RecurringSummaryChip(label = filter.type.label())
        RecurringSummaryChip(label = filter.status.label())
        if (filter.selectedFrequencies.isEmpty()) {
            RecurringSummaryChip(label = stringResource(R.string.all_frequencies))
        } else {
            filter.selectedFrequencies.sortedBy { it.ordinal }.forEach { frequency ->
                RecurringSummaryChip(label = frequency.label())
            }
        }
        if (selectedCategories.isEmpty()) {
            RecurringSummaryChip(
                label = stringResource(R.string.all_categories),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Category,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        } else {
            selectedCategories.forEach { (_, category) ->
                RecurringSummaryChip(
                    label = category?.title ?: stringResource(R.string.uncategorized),
                    leadingIcon = {
                        CategoryIconCircle(
                            iconName = category?.iconName ?: "category",
                            color = category?.toColor() ?: MaterialTheme.colorScheme.primary,
                            contentDescription = null,
                            size = 20.dp,
                            iconSize = 13.dp,
                        )
                    },
                )
            }
        }
        if (filter.nextRunStartDate != null || filter.nextRunEndDate != null) {
            RecurringSummaryChip(
                label = stringResource(
                    R.string.recurring_next_run_filter_summary,
                    filter.nextRunStartDate?.toString() ?: stringResource(R.string.any_date),
                    filter.nextRunEndDate?.toString() ?: stringResource(R.string.any_date),
                ),
            )
        }
    }
}

@Composable
private fun RecurringSummaryChip(
    label: String,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    androidx.compose.material3.Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 32.dp)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            leadingIcon?.invoke()
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun RecurringFiltersSheet(
    categories: List<CategoryEntity>,
    defaultFilter: RecurringTransactionsFilter,
    initialFilter: RecurringTransactionsFilter,
    onApply: (RecurringTransactionsFilter) -> Unit,
    onDismiss: () -> Unit,
    onOpenCategoryFilter: () -> Unit,
    showCategoryFilterSheet: Boolean,
    onCategoryFilterDismiss: () -> Unit,
) {
    val strings = RecurringFiltersStrings(
        filters = stringResource(R.string.filters),
        transactionType = stringResource(R.string.transaction_type),
        expense = stringResource(R.string.expense),
        income = stringResource(R.string.income),
        both = stringResource(R.string.both),
        frequency = stringResource(R.string.frequency),
        daily = stringResource(R.string.frequency_daily),
        weekly = stringResource(R.string.frequency_weekly),
        monthly = stringResource(R.string.frequency_monthly),
        yearly = stringResource(R.string.frequency_yearly),
        status = stringResource(R.string.status),
        allStatuses = stringResource(R.string.all_statuses),
        active = stringResource(R.string.active),
        inactive = stringResource(R.string.inactive),
        nextRunDate = stringResource(R.string.next_run_date),
        startDate = stringResource(R.string.start_date),
        endDate = stringResource(R.string.end_date),
        allCategories = stringResource(R.string.all_categories),
        selectedCategoriesCount = { count -> stringResource(R.string.selected_categories_count, count) },
        resetFilters = stringResource(R.string.reset_filters),
        cancel = stringResource(R.string.cancel),
        apply = stringResource(R.string.apply),
        anyDate = stringResource(R.string.any_date),
        clearDate = stringResource(R.string.clear_date),
        categories = stringResource(R.string.categories),
        noCategoriesYet = stringResource(R.string.no_categories_yet),
    )
    var type by rememberSaveable(initialFilter) { mutableStateOf(initialFilter.type) }
    var selectedCategoryIds by rememberSaveable(initialFilter) {
        mutableStateOf(initialFilter.selectedCategoryIds.toList())
    }
    var selectedFrequencies by rememberSaveable(initialFilter) {
        mutableStateOf(initialFilter.selectedFrequencies.toList())
    }
    var status by rememberSaveable(initialFilter) { mutableStateOf(initialFilter.status) }
    var startDate by rememberSaveable(initialFilter) { mutableStateOf(initialFilter.nextRunStartDate) }
    var endDate by rememberSaveable(initialFilter) { mutableStateOf(initialFilter.nextRunEndDate) }
    val availableCategories = remember(categories, type) {
        categories.availableForRecurringType(type)
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
                text = strings.filters,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = strings.transactionType,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RecurringTransactionTypeFilter.entries.forEach { option ->
                    FilterChip(
                        selected = type == option,
                        onClick = { type = option },
                        label = { Text(option.label(strings)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Text(
                text = strings.frequency,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RecurringFrequency.entries.forEach { frequency ->
                    val selected = frequency in selectedFrequencies
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedFrequencies = if (selected) {
                                selectedFrequencies - frequency
                            } else {
                                selectedFrequencies + frequency
                            }
                        },
                        label = { Text(frequency.label(strings)) },
                    )
                }
            }
            Text(
                text = strings.status,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RecurringStatusFilter.entries.forEach { option ->
                    FilterChip(
                        selected = status == option,
                        onClick = { status = option },
                        label = { Text(option.label(strings)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Text(
                text = strings.nextRunDate,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RecurringNullableDateButton(
                    label = strings.startDate,
                    date = startDate,
                    modifier = Modifier.weight(1f),
                    onDateSelected = { selectedDate ->
                        startDate = selectedDate
                        if (endDate != null && endDate!!.isBefore(selectedDate)) {
                            endDate = selectedDate
                        }
                    },
                    onClear = { startDate = null },
                    strings = strings,
                )
                RecurringNullableDateButton(
                    label = strings.endDate,
                    date = endDate,
                    modifier = Modifier.weight(1f),
                    onDateSelected = { selectedDate ->
                        endDate = selectedDate
                        if (startDate != null && startDate!!.isAfter(selectedDate)) {
                            startDate = selectedDate
                        }
                    },
                    onClear = { endDate = null },
                    strings = strings,
                )
            }
            FilledTonalButton(
                onClick = onOpenCategoryFilter,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (selectedCategoryIds.isEmpty()) {
                        strings.allCategories
                    } else {
                        strings.selectedCategoriesCount(selectedCategoryIds.size)
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = {
                        type = defaultFilter.type
                        selectedCategoryIds = emptyList()
                        selectedFrequencies = emptyList()
                        status = defaultFilter.status
                        startDate = null
                        endDate = null
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(strings.resetFilters)
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(strings.cancel)
                }
                Button(
                    onClick = {
                        onApply(
                            RecurringTransactionsFilter(
                                type = type,
                                selectedCategoryIds = selectedCategoryIds.toSet(),
                                selectedFrequencies = selectedFrequencies.toSet(),
                                status = status,
                                nextRunStartDate = startDate,
                                nextRunEndDate = endDate,
                            ),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(strings.apply)
                }
            }
        }
    }

    if (showCategoryFilterSheet) {
        RecurringCategoryFilterSheet(
            categories = availableCategories,
            selectedCategoryIds = selectedCategoryIds.toSet(),
            onSelectionChanged = { selectedCategoryIds = it.toList() },
            onDismiss = onCategoryFilterDismiss,
            strings = strings,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringCategoryFilterSheet(
    categories: List<CategoryEntity>,
    selectedCategoryIds: Set<Long>,
    onSelectionChanged: (Set<Long>) -> Unit,
    onDismiss: () -> Unit,
    strings: RecurringFiltersStrings,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = strings.categories,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (categories.isEmpty()) {
                EmptyState(text = strings.noCategoriesYet)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 460.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    gridItems(
                        items = categories,
                        key = { it.id },
                        contentType = { "category" },
                    ) { category ->
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
                    Text(strings.allCategories)
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(strings.apply)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecurringTransactionEditorSheet(
    rule: RecurringTransactionEntity?,
    selectedBudgetBookId: Long?,
    categories: List<CategoryEntity>,
    clock: Clock,
    onDismiss: () -> Unit,
    onSave: suspend (RecurringTransactionEntity) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = RecurringEditorStrings(
        addTitle = stringResource(R.string.add_recurring_transaction),
        editTitle = stringResource(R.string.edit_recurring_transaction),
        amount = stringResource(R.string.amount),
        expense = stringResource(R.string.expense),
        income = stringResource(R.string.income),
        category = stringResource(R.string.category),
        noCategoriesYet = stringResource(R.string.no_categories_yet),
        titleOptional = stringResource(R.string.title_optional),
        noteOptional = stringResource(R.string.note_optional),
        frequency = stringResource(R.string.frequency),
        daily = stringResource(R.string.frequency_daily),
        weekly = stringResource(R.string.frequency_weekly),
        monthly = stringResource(R.string.frequency_monthly),
        yearly = stringResource(R.string.frequency_yearly),
        interval = stringResource(R.string.interval),
        weekday = stringResource(R.string.weekday),
        monthDay = stringResource(R.string.month_day),
        endDateOptional = stringResource(R.string.end_date_optional),
        active = stringResource(R.string.active),
        activeHelper = stringResource(R.string.recurring_active_helper),
        everyDay = stringResource(R.string.recurring_every_day),
        every2Days = stringResource(R.string.recurring_every_2_days),
        every3Days = stringResource(R.string.recurring_every_3_days),
        everyWeek = stringResource(R.string.recurring_every_week),
        every2Weeks = stringResource(R.string.recurring_every_2_weeks),
        every3Weeks = stringResource(R.string.recurring_every_3_weeks),
        everyMonth = stringResource(R.string.recurring_every_month),
        every2Months = stringResource(R.string.recurring_every_2_months),
        every3Months = stringResource(R.string.recurring_every_3_months),
        everyYear = stringResource(R.string.recurring_every_year),
        monday = stringResource(R.string.monday),
        tuesday = stringResource(R.string.tuesday),
        wednesday = stringResource(R.string.wednesday),
        thursday = stringResource(R.string.thursday),
        friday = stringResource(R.string.friday),
        saturday = stringResource(R.string.saturday),
        sunday = stringResource(R.string.sunday),
        saveRecurringTransaction = stringResource(R.string.save_recurring_transaction),
        updateRecurringTransaction = stringResource(R.string.update_recurring_transaction),
        apply = stringResource(R.string.apply),
        cancel = stringResource(R.string.cancel),
        anyDate = stringResource(R.string.any_date),
        clearDate = stringResource(R.string.clear_date),
    )
    val today = remember(clock) { LocalDate.now(clock) }
    var amountText by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.amountMinor?.let(MoneyFormatter::formatAmount) ?: "")
    }
    var transactionType by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.type ?: TransactionType.EXPENSE)
    }
    var selectedCategoryId by rememberSaveable(rule?.id, transactionType) {
        mutableStateOf(rule?.categoryId)
    }
    var titleText by rememberSaveable(rule?.id) { mutableStateOf(rule?.title.orEmpty()) }
    var noteText by rememberSaveable(rule?.id) { mutableStateOf(rule?.note.orEmpty()) }
    var frequency by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.frequency ?: RecurringFrequency.MONTHLY)
    }
    var interval by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.interval ?: RecurringSchedule.MinInterval)
    }
    var selectedWeekday by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.scheduleWeekday ?: rule?.startDate?.dayOfWeek?.value ?: today.dayOfWeek.value)
    }
    var selectedMonthDay by rememberSaveable(rule?.id) {
        mutableStateOf(
            (rule?.scheduleMonthDay ?: rule?.startDate?.dayOfMonth ?: today.dayOfMonth)
                .coerceIn(RecurringSchedule.MinMonthDay, RecurringSchedule.MaxMonthDay),
        )
    }
    var endDate by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.endDate ?: if (rule == null) today.plusYears(10) else null)
    }
    var isActive by rememberSaveable(rule?.id) { mutableStateOf(rule?.isActive ?: true) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val availableCategories = remember(categories, transactionType) {
        categories.availableForTransactionType(transactionType)
    }
    LaunchedEffect(availableCategories) {
        val availableIds = availableCategories.map { it.id }.toSet()
        if (selectedCategoryId == null && rule == null) {
            selectedCategoryId = availableCategories.firstOrNull()?.id
        } else if (selectedCategoryId != null && selectedCategoryId !in availableIds) {
            selectedCategoryId = availableCategories.firstOrNull()?.id
        }
    }
    val invalidAmountMessage = stringResource(R.string.amount_error)
    val missingCategoryMessage = stringResource(R.string.category_error)
    val missingBookMessage = stringResource(R.string.loading_budget_book)
    val invalidDateMessage = stringResource(R.string.recurring_date_error)
    val saveFailedMessage = stringResource(R.string.recurring_transaction_save_error)
    val savedMessage = stringResource(R.string.recurring_transaction_saved)
    val updatedMessage = stringResource(R.string.recurring_transaction_updated)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text(
                        text = if (rule == null) {
                            strings.addTitle
                        } else {
                            strings.editTitle
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                item {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.toAmountInputText() },
                        label = { Text(strings.amount) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recurring_amount"),
                    )
                }
                item {
                    CategoryTypeSwitch(
                        selectedType = when (transactionType) {
                            TransactionType.EXPENSE -> CategoryType.EXPENSE
                            TransactionType.INCOME -> CategoryType.INCOME
                        },
                        onTypeSelected = { selectedType ->
                            transactionType = when (selectedType) {
                                CategoryType.EXPENSE -> TransactionType.EXPENSE
                                CategoryType.INCOME -> TransactionType.INCOME
                                CategoryType.BOTH -> transactionType
                            }
                        },
                        labels = CategoryTypeSwitchLabels(
                            expense = strings.expense,
                            income = strings.income,
                        ),
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = strings.category,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (availableCategories.isEmpty()) {
                            EmptyState(text = strings.noCategoriesYet)
                        } else {
                            LazyHorizontalGrid(
                                rows = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(292.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(vertical = 2.dp),
                            ) {
                                gridItems(
                                    items = availableCategories,
                                    key = { it.id },
                                    contentType = { "category" },
                                ) { category ->
                                    CategoryGridTile(
                                        category = category,
                                        onClick = { selectedCategoryId = category.id },
                                        selected = selectedCategoryId == category.id,
                                        modifier = Modifier
                                            .width(112.dp)
                                            .testTag("recurring_category_${category.id}"),
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text(strings.titleOptional) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text(strings.noteOptional) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RecurringFrequencyDropdown(
                            frequency = frequency,
                            onFrequencySelected = { selectedFrequency ->
                                frequency = selectedFrequency
                                selectedWeekday = selectedWeekday.coerceIn(1, 7)
                                selectedMonthDay = selectedMonthDay.coerceIn(
                                    RecurringSchedule.MinMonthDay,
                                    RecurringSchedule.MaxMonthDay,
                                )
                            },
                            modifier = Modifier.weight(1f),
                            strings = strings,
                        )
                        RecurringIntervalDropdown(
                            frequency = frequency,
                            interval = interval,
                            onIntervalSelected = { interval = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("recurring_interval"),
                            strings = strings,
                        )
                    }
                }
                if (frequency == RecurringFrequency.WEEKLY) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            WeekdayDropdown(
                                selectedWeekday = selectedWeekday,
                                onWeekdaySelected = { selectedWeekday = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("recurring_weekday"),
                                strings = strings,
                            )
                            RecurringNullableDateButton(
                                label = strings.endDateOptional,
                                date = endDate,
                                onDateSelected = { selectedDate -> endDate = selectedDate },
                                onClear = { endDate = null },
                                modifier = Modifier.weight(1f),
                                strings = strings,
                            )
                        }
                    }
                } else if (frequency == RecurringFrequency.MONTHLY) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MonthDayDropdown(
                                selectedDay = selectedMonthDay,
                                onDaySelected = { selectedMonthDay = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("recurring_month_day"),
                                strings = strings,
                            )
                            RecurringNullableDateButton(
                                label = strings.endDateOptional,
                                date = endDate,
                                onDateSelected = { selectedDate -> endDate = selectedDate },
                                onClear = { endDate = null },
                                modifier = Modifier.weight(1f),
                                strings = strings,
                            )
                        }
                    }
                } else {
                    item {
                        RecurringNullableDateButton(
                            label = strings.endDateOptional,
                            date = endDate,
                            onDateSelected = { selectedDate -> endDate = selectedDate },
                            onClear = { endDate = null },
                            modifier = Modifier.fillMaxWidth(),
                            strings = strings,
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.active,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = strings.activeHelper,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            modifier = Modifier.testTag("recurring_active"),
                        )
                    }
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
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .imePadding(),
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
                Button(
                    onClick = {
                        if (isSaving) return@Button
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
                        val scheduleWeekday = RecurringSchedule.normalizedWeekday(frequency, selectedWeekday)
                        val scheduleMonthDay = RecurringSchedule.normalizedMonthDay(frequency, selectedMonthDay)
                        val shouldReanchor = rule == null ||
                            frequency != rule.frequency ||
                            interval != rule.interval ||
                            scheduleWeekday != rule.effectiveScheduleWeekday() ||
                            scheduleMonthDay != rule.effectiveScheduleMonthDay()
                        val computedStartDate = if (shouldReanchor) {
                            RecurringSchedule.nextStartDate(
                                frequency = frequency,
                                today = today,
                                scheduleWeekday = scheduleWeekday,
                                scheduleMonthDay = scheduleMonthDay,
                            )
                        } else {
                            rule.startDate
                        }
                        val computedNextRunDate = if (shouldReanchor) computedStartDate else rule.nextRunDate
                        if (endDate != null && endDate!!.isBefore(computedStartDate)) {
                            errorText = invalidDateMessage
                            return@Button
                        }
                        val categoryId = selectedCategoryId
                        if (rule == null && categoryId == null) {
                            errorText = missingCategoryMessage
                            return@Button
                        }
                        errorText = null
                        isSaving = true
                        scope.launch {
                            try {
                                val now = clock.instant()
                                onSave(
                                    RecurringTransactionEntity(
                                        id = rule?.id ?: 0L,
                                        budgetBookId = rule?.budgetBookId ?: budgetBookId,
                                        categoryId = categoryId,
                                        type = transactionType,
                                        amountMinor = amountMinor,
                                        title = titleText.trim().ifBlank { null },
                                        note = noteText.trim().ifBlank { null },
                                        frequency = frequency,
                                        interval = interval,
                                        scheduleWeekday = scheduleWeekday,
                                        scheduleMonthDay = scheduleMonthDay,
                                        startDate = computedStartDate,
                                        endDate = endDate,
                                        nextRunDate = computedNextRunDate,
                                        lastRunDate = rule?.lastRunDate,
                                        isActive = isActive,
                                        createdAt = rule?.createdAt ?: now,
                                        updatedAt = rule?.updatedAt ?: now,
                                    ),
                                )
                                onDismiss()
                                snackbarHostState.showSnackbar(
                                    if (rule == null) savedMessage else updatedMessage,
                                )
                            } catch (cancellation: CancellationException) {
                                isSaving = false
                                throw cancellation
                            } catch (_: Exception) {
                                isSaving = false
                                snackbarHostState.showSnackbar(saveFailedMessage)
                            }
                        }
                    },
                    enabled = amountText.isNotBlank() && !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .testTag("recurring_save"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        if (rule == null) {
                            strings.saveRecurringTransaction
                        } else {
                            strings.updateRecurringTransaction
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringFrequencyDropdown(
    frequency: RecurringFrequency,
    onFrequencySelected: (RecurringFrequency) -> Unit,
    modifier: Modifier = Modifier,
    strings: RecurringEditorStrings,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = frequency.label(strings),
            onValueChange = {},
            readOnly = true,
            label = { Text(strings.frequency) },
            leadingIcon = {
                Icon(imageVector = Icons.Filled.EventRepeat, contentDescription = null)
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            val options = if (frequency == RecurringFrequency.YEARLY) {
                listOf(RecurringFrequency.YEARLY, RecurringFrequency.DAILY, RecurringFrequency.WEEKLY, RecurringFrequency.MONTHLY)
            } else {
                listOf(RecurringFrequency.DAILY, RecurringFrequency.WEEKLY, RecurringFrequency.MONTHLY)
            }
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label(strings)) },
                    onClick = {
                        onFrequencySelected(option)
                        expanded = false
                    },
                    modifier = Modifier.testTag("recurring_frequency_${option.name.lowercase()}"),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringIntervalDropdown(
    frequency: RecurringFrequency,
    interval: Int,
    onIntervalSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    strings: RecurringEditorStrings,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = frequency.intervalLabel(interval, strings),
            onValueChange = {},
            readOnly = true,
            label = { Text(strings.interval) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            (RecurringSchedule.MinInterval..RecurringSchedule.MaxSimpleInterval).forEach { option ->
                DropdownMenuItem(
                    text = { Text(frequency.intervalLabel(option, strings)) },
                    onClick = {
                        onIntervalSelected(option)
                        expanded = false
                    },
                    modifier = Modifier.testTag("recurring_interval_$option"),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekdayDropdown(
    selectedWeekday: Int,
    onWeekdaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    strings: RecurringEditorStrings,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedDay = DayOfWeek.entries.firstOrNull { it.value == selectedWeekday } ?: DayOfWeek.MONDAY
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedDay.label(strings),
            onValueChange = {},
            readOnly = true,
            label = { Text(strings.weekday) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            WeekdayDropdownOrder.forEach { day ->
                DropdownMenuItem(
                    text = { Text(day.label(strings)) },
                    onClick = {
                        onWeekdaySelected(day.value)
                        expanded = false
                    },
                    modifier = Modifier.testTag("recurring_weekday_${day.value}"),
                )
            }
        }
    }
}

private val WeekdayDropdownOrder = listOf(
    DayOfWeek.SUNDAY,
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthDayDropdown(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    strings: RecurringEditorStrings,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedDay.coerceIn(
                RecurringSchedule.MinMonthDay,
                RecurringSchedule.MaxMonthDay,
            ).toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text(strings.monthDay) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            (RecurringSchedule.MinMonthDay..RecurringSchedule.MaxMonthDay).forEach { day ->
                DropdownMenuItem(
                    text = { Text(day.toString()) },
                    onClick = {
                        onDaySelected(day)
                        expanded = false
                    },
                    modifier = Modifier.testTag("recurring_month_day_$day"),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringDateButton(
    label: String,
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    applyLabel: String,
    cancelLabel: String,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    FilledTonalButton(
        onClick = { showPicker = true },
        modifier = modifier.heightIn(min = 56.dp),
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
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
                    Text(applyLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(cancelLabel)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun RecurringNullableDateButton(
    label: String,
    date: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    strings: RecurringSheetDateStrings,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        RecurringDateButton(
            label = label,
            date = date ?: LocalDate.now(),
            onDateSelected = onDateSelected,
            modifier = Modifier.fillMaxWidth(),
            applyLabel = strings.apply,
            cancelLabel = strings.cancel,
        )
        if (date == null) {
            Text(
                text = strings.anyDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            TextButton(onClick = onClear) {
                Text(strings.clearDate)
            }
        }
    }
}

internal enum class RecurringTransactionTypeFilter {
    Expense,
    Income,
    Both,
}

internal enum class RecurringStatusFilter {
    All,
    Active,
    Inactive,
}

private interface RecurringSheetDateStrings {
    val apply: String
    val cancel: String
    val anyDate: String
    val clearDate: String
}

private data class RecurringFiltersStrings(
    val filters: String,
    val transactionType: String,
    val expense: String,
    val income: String,
    val both: String,
    val frequency: String,
    val daily: String,
    val weekly: String,
    val monthly: String,
    val yearly: String,
    val status: String,
    val allStatuses: String,
    val active: String,
    val inactive: String,
    val nextRunDate: String,
    val startDate: String,
    val endDate: String,
    val allCategories: String,
    val selectedCategoriesCount: @Composable (Int) -> String,
    val resetFilters: String,
    override val cancel: String,
    override val apply: String,
    override val anyDate: String,
    override val clearDate: String,
    val categories: String,
    val noCategoriesYet: String,
) : RecurringSheetDateStrings

private data class RecurringEditorStrings(
    val addTitle: String,
    val editTitle: String,
    val amount: String,
    val expense: String,
    val income: String,
    val category: String,
    val noCategoriesYet: String,
    val titleOptional: String,
    val noteOptional: String,
    val frequency: String,
    val daily: String,
    val weekly: String,
    val monthly: String,
    val yearly: String,
    val interval: String,
    val weekday: String,
    val monthDay: String,
    val endDateOptional: String,
    val active: String,
    val activeHelper: String,
    val everyDay: String,
    val every2Days: String,
    val every3Days: String,
    val everyWeek: String,
    val every2Weeks: String,
    val every3Weeks: String,
    val everyMonth: String,
    val every2Months: String,
    val every3Months: String,
    val everyYear: String,
    val monday: String,
    val tuesday: String,
    val wednesday: String,
    val thursday: String,
    val friday: String,
    val saturday: String,
    val sunday: String,
    val saveRecurringTransaction: String,
    val updateRecurringTransaction: String,
    override val apply: String,
    override val cancel: String,
    override val anyDate: String,
    override val clearDate: String,
) : RecurringSheetDateStrings

internal data class RecurringTransactionsFilter(
    val type: RecurringTransactionTypeFilter = RecurringTransactionTypeFilter.Both,
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedFrequencies: Set<RecurringFrequency> = emptySet(),
    val status: RecurringStatusFilter = RecurringStatusFilter.All,
    val nextRunStartDate: LocalDate? = null,
    val nextRunEndDate: LocalDate? = null,
)

internal fun List<RecurringTransactionEntity>.filterByRecurringTransactionsFilter(
    filter: RecurringTransactionsFilter,
): List<RecurringTransactionEntity> =
    filter { rule ->
        val matchesType = when (filter.type) {
            RecurringTransactionTypeFilter.Expense -> rule.type == TransactionType.EXPENSE
            RecurringTransactionTypeFilter.Income -> rule.type == TransactionType.INCOME
            RecurringTransactionTypeFilter.Both -> true
        }
        val matchesCategory = filter.selectedCategoryIds.isEmpty() ||
            rule.categoryId in filter.selectedCategoryIds
        val matchesFrequency = filter.selectedFrequencies.isEmpty() ||
            rule.frequency in filter.selectedFrequencies
        val matchesStatus = when (filter.status) {
            RecurringStatusFilter.All -> true
            RecurringStatusFilter.Active -> rule.isActive
            RecurringStatusFilter.Inactive -> !rule.isActive
        }
        val matchesNextRunStart = filter.nextRunStartDate == null ||
            !rule.nextRunDate.isBefore(filter.nextRunStartDate)
        val matchesNextRunEnd = filter.nextRunEndDate == null ||
            !rule.nextRunDate.isAfter(filter.nextRunEndDate)
        matchesType && matchesCategory && matchesFrequency && matchesStatus &&
            matchesNextRunStart && matchesNextRunEnd
    }

@Composable
private fun RecurringTransactionTypeFilter.label(): String = when (this) {
    RecurringTransactionTypeFilter.Expense -> stringResource(R.string.expense)
    RecurringTransactionTypeFilter.Income -> stringResource(R.string.income)
    RecurringTransactionTypeFilter.Both -> stringResource(R.string.both)
}

private fun RecurringTransactionTypeFilter.label(strings: RecurringFiltersStrings): String = when (this) {
    RecurringTransactionTypeFilter.Expense -> strings.expense
    RecurringTransactionTypeFilter.Income -> strings.income
    RecurringTransactionTypeFilter.Both -> strings.both
}

@Composable
private fun RecurringStatusFilter.label(): String = when (this) {
    RecurringStatusFilter.All -> stringResource(R.string.all_statuses)
    RecurringStatusFilter.Active -> stringResource(R.string.active)
    RecurringStatusFilter.Inactive -> stringResource(R.string.inactive)
}

private fun RecurringStatusFilter.label(strings: RecurringFiltersStrings): String = when (this) {
    RecurringStatusFilter.All -> strings.allStatuses
    RecurringStatusFilter.Active -> strings.active
    RecurringStatusFilter.Inactive -> strings.inactive
}

@Composable
private fun RecurringFrequency.label(): String = when (this) {
    RecurringFrequency.DAILY -> stringResource(R.string.frequency_daily)
    RecurringFrequency.WEEKLY -> stringResource(R.string.frequency_weekly)
    RecurringFrequency.MONTHLY -> stringResource(R.string.frequency_monthly)
    RecurringFrequency.YEARLY -> stringResource(R.string.frequency_yearly)
}

private fun RecurringFrequency.label(strings: RecurringFiltersStrings): String = when (this) {
    RecurringFrequency.DAILY -> strings.daily
    RecurringFrequency.WEEKLY -> strings.weekly
    RecurringFrequency.MONTHLY -> strings.monthly
    RecurringFrequency.YEARLY -> strings.yearly
}

private fun RecurringFrequency.label(strings: RecurringEditorStrings): String = when (this) {
    RecurringFrequency.DAILY -> strings.daily
    RecurringFrequency.WEEKLY -> strings.weekly
    RecurringFrequency.MONTHLY -> strings.monthly
    RecurringFrequency.YEARLY -> strings.yearly
}

@Composable
private fun RecurringFrequency.intervalLabel(
    interval: Int,
    strings: RecurringEditorStrings,
): String = when (this) {
    RecurringFrequency.DAILY -> if (interval == 1) {
        strings.everyDay
    } else if (interval == 2) {
        strings.every2Days
    } else if (interval == 3) {
        strings.every3Days
    } else {
        stringResource(R.string.recurring_every_n_days, interval)
    }
    RecurringFrequency.WEEKLY -> if (interval == 1) {
        strings.everyWeek
    } else if (interval == 2) {
        strings.every2Weeks
    } else if (interval == 3) {
        strings.every3Weeks
    } else {
        stringResource(R.string.recurring_every_n_weeks, interval)
    }
    RecurringFrequency.MONTHLY -> if (interval == 1) {
        strings.everyMonth
    } else if (interval == 2) {
        strings.every2Months
    } else if (interval == 3) {
        strings.every3Months
    } else {
        stringResource(R.string.recurring_every_n_months, interval)
    }
    RecurringFrequency.YEARLY -> if (interval == 1) {
        strings.everyYear
    } else {
        stringResource(R.string.recurring_every_n_years, interval)
    }
}

private fun DayOfWeek.label(strings: RecurringEditorStrings): String = when (this) {
    DayOfWeek.MONDAY -> strings.monday
    DayOfWeek.TUESDAY -> strings.tuesday
    DayOfWeek.WEDNESDAY -> strings.wednesday
    DayOfWeek.THURSDAY -> strings.thursday
    DayOfWeek.FRIDAY -> strings.friday
    DayOfWeek.SATURDAY -> strings.saturday
    DayOfWeek.SUNDAY -> strings.sunday
}

private fun RecurringTransactionEntity.effectiveScheduleWeekday(): Int? =
    if (frequency == RecurringFrequency.WEEKLY) {
        scheduleWeekday ?: startDate.dayOfWeek.value
    } else {
        null
    }

private fun RecurringTransactionEntity.effectiveScheduleMonthDay(): Int? =
    if (frequency == RecurringFrequency.MONTHLY) {
        scheduleMonthDay ?: startDate.dayOfMonth.coerceAtMost(RecurringSchedule.MaxMonthDay)
    } else {
        null
    }

private fun List<CategoryEntity>.availableForRecurringType(
    type: RecurringTransactionTypeFilter,
): List<CategoryEntity> =
    filter { category ->
        category.archivedAt == null && when (type) {
            RecurringTransactionTypeFilter.Expense ->
                category.type == CategoryType.EXPENSE || category.type == CategoryType.BOTH
            RecurringTransactionTypeFilter.Income ->
                category.type == CategoryType.INCOME || category.type == CategoryType.BOTH
            RecurringTransactionTypeFilter.Both -> true
        }
    }.sortedWith(compareBy<CategoryEntity> { it.sortOrder }.thenBy { it.title })

private fun List<CategoryEntity>.availableForTransactionType(
    type: TransactionType,
): List<CategoryEntity> =
    filter { category ->
        category.archivedAt == null && when (type) {
            TransactionType.EXPENSE -> category.type == CategoryType.EXPENSE || category.type == CategoryType.BOTH
            TransactionType.INCOME -> category.type == CategoryType.INCOME || category.type == CategoryType.BOTH
        }
    }.sortedWith(compareBy<CategoryEntity> { it.sortOrder }.thenBy { it.title })

private fun LocalDate.toPickerMillis(): Long =
    atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toPickerDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun String.toAmountInputText(): String {
    val allowed = filter { it.isDigit() || it == '.' }
    val decimalIndex = allowed.indexOf('.')
    if (decimalIndex == -1) {
        return allowed
    }

    val wholePart = allowed.take(decimalIndex)
    val decimalPart = allowed
        .drop(decimalIndex + 1)
        .filter { it != '.' }
        .take(2)
    return "$wholePart.$decimalPart"
}
