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
import com.ozcomingfroo.mybudget.ui.theme.ExpenseRed
import com.ozcomingfroo.mybudget.ui.theme.IncomeGreen
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun RecurringTransactionsScreen(
    selectedBudgetBookId: Long?,
    categories: List<CategoryEntity>,
    recurringTransactions: List<RecurringTransactionEntity>,
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
    val categoryById = categories.associateBy { it.id }
    val activeFilter = RecurringTransactionsFilter(
        type = typeFilter,
        selectedCategoryIds = selectedCategoryIds.toSet(),
        selectedFrequencies = selectedFrequencies.toSet(),
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
            items(filteredRules, key = { it.id }) { rule ->
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
        } else {
            stringResource(R.string.recurring_every_n_days, interval)
        }
        RecurringFrequency.WEEKLY -> if (interval == 1) {
            stringResource(R.string.recurring_every_week)
        } else {
            stringResource(R.string.recurring_every_n_weeks, interval)
        }
        RecurringFrequency.MONTHLY -> if (interval == 1) {
            stringResource(R.string.recurring_every_month)
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
        startDate = stringResource(R.string.start_date),
        nextRunDate = stringResource(R.string.next_run_date),
        endDateOptional = stringResource(R.string.end_date_optional),
        active = stringResource(R.string.active),
        activeHelper = stringResource(R.string.recurring_active_helper),
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
    var intervalText by rememberSaveable(rule?.id) {
        mutableStateOf((rule?.interval ?: 1).toString())
    }
    var startDate by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.startDate ?: today)
    }
    var nextRunDate by rememberSaveable(rule?.id) {
        mutableStateOf(rule?.nextRunDate ?: today)
    }
    var endDate by rememberSaveable(rule?.id) { mutableStateOf(rule?.endDate) }
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
    val invalidIntervalMessage = stringResource(R.string.recurring_interval_error)
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
                                gridItems(availableCategories, key = { it.id }) { category ->
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
                            onFrequencySelected = { frequency = it },
                            modifier = Modifier.weight(1f),
                            strings = strings,
                        )
                        OutlinedTextField(
                            value = intervalText,
                            onValueChange = { intervalText = it.filter(Char::isDigit).take(3) },
                            label = { Text(strings.interval) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("recurring_interval"),
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RecurringDateButton(
                            label = strings.startDate,
                            date = startDate,
                            modifier = Modifier.weight(1f),
                            onDateSelected = { selectedDate ->
                                startDate = selectedDate
                                if (nextRunDate.isBefore(selectedDate)) {
                                    nextRunDate = selectedDate
                                }
                                if (endDate != null && endDate!!.isBefore(selectedDate)) {
                                    endDate = selectedDate
                                }
                            },
                            applyLabel = strings.apply,
                            cancelLabel = strings.cancel,
                        )
                        RecurringDateButton(
                            label = strings.nextRunDate,
                            date = nextRunDate,
                            modifier = Modifier.weight(1f),
                            onDateSelected = { selectedDate -> nextRunDate = selectedDate },
                            applyLabel = strings.apply,
                            cancelLabel = strings.cancel,
                        )
                    }
                }
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
                        val interval = intervalText.toIntOrNull()
                        if (interval == null || interval < 1) {
                            errorText = invalidIntervalMessage
                            return@Button
                        }
                        if (nextRunDate.isBefore(startDate) || (endDate != null && endDate!!.isBefore(startDate))) {
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
                                        startDate = startDate,
                                        endDate = endDate,
                                        nextRunDate = nextRunDate,
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
            RecurringFrequency.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label(strings)) },
                    onClick = {
                        onFrequencySelected(option)
                        expanded = false
                    },
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
    val startDate: String,
    val nextRunDate: String,
    val endDateOptional: String,
    val active: String,
    val activeHelper: String,
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
