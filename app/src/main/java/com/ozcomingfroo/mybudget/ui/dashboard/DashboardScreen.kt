package com.ozcomingfroo.mybudget.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.core.money.MoneyFormatter
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import com.ozcomingfroo.mybudget.ui.theme.ExpenseRed
import com.ozcomingfroo.mybudget.ui.theme.IncomeGreen
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
internal fun DashboardScreen(
    selectedBudgetBookId: Long?,
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    clock: Clock,
    updateTransaction: suspend (TransactionEntity) -> Unit,
    snackbarHostState: SnackbarHostState,
    onAddTransaction: () -> Unit,
    onViewHistory: () -> Unit,
    onOpenReports: () -> Unit,
) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    var rangeType by rememberSaveable { mutableStateOf(DashboardRangeType.Month) }
    var anchorEpochDay by rememberSaveable { mutableLongStateOf(LocalDate.now(clock).toEpochDay()) }
    val anchorDate = LocalDate.ofEpochDay(anchorEpochDay)
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    val dateRange = remember(rangeType, anchorDate, locale) {
        DashboardDateRange.from(rangeType, anchorDate, locale)
    }
    val rangeTransactions = remember(transactions, dateRange) {
        transactions.filter { transaction ->
            val date = transaction.occurredAt.toLocalDate()
            !date.isBefore(dateRange.startDate) && date.isBefore(dateRange.endExclusiveDate)
        }
    }
    val income = rangeTransactions.total(TransactionType.INCOME)
    val expenses = rangeTransactions.total(TransactionType.EXPENSE)
    val remaining = income - expenses
    val categoryById = categories.associateBy { it.id }
    val incomeSlices = rangeTransactions.categorySlices(
        type = TransactionType.INCOME,
        categoryById = categoryById,
        uncategorizedLabel = stringResource(R.string.uncategorized),
        fallbackColor = MaterialTheme.colorScheme.primary,
    )
    val expenseSlices = rangeTransactions.categorySlices(
        type = TransactionType.EXPENSE,
        categoryById = categoryById,
        uncategorizedLabel = stringResource(R.string.uncategorized),
        fallbackColor = MaterialTheme.colorScheme.primary,
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            DashboardRangeSelector(
                selectedType = rangeType,
                dateRange = dateRange,
                locale = locale,
                onTypeSelected = {
                    rangeType = it
                    anchorEpochDay = LocalDate.now(clock).toEpochDay()
                },
                onPrevious = { anchorEpochDay = rangeType.move(anchorDate, -1).toEpochDay() },
                onNext = { anchorEpochDay = rangeType.move(anchorDate, 1).toEpochDay() },
            )
        }
        item {
            DonutMetricCard(
                title = stringResource(R.string.remaining_for_range),
                amountMinor = remaining,
                supportingText = stringResource(R.string.income_minus_expenses),
                slices = listOf(
                    DonutSlice(stringResource(R.string.income), income, IncomeGreen),
                    DonutSlice(stringResource(R.string.expenses), expenses, ExpenseRed),
                ),
                emphasized = true,
                modifier = Modifier.testTag("dashboard_remaining_donut"),
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DonutMetricCard(
                    title = stringResource(R.string.income),
                    amountMinor = income,
                    slices = incomeSlices,
                    amountColor = IncomeGreen,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_income_donut"),
                )
                DonutMetricCard(
                    title = stringResource(R.string.expenses),
                    amountMinor = expenses,
                    slices = expenseSlices,
                    amountColor = ExpenseRed,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_expense_donut"),
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onAddTransaction() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.add_expense))
                }
                FilledTonalButton(
                    onClick = { onAddTransaction() },
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
                onAction = { onViewHistory() },
            )
        }
        if (selectedBudgetBookId == null) {
            item { EmptyState(text = stringResource(R.string.loading_budget_book)) }
        } else if (rangeTransactions.isEmpty()) {
            item {
                EmptyState(
                    text = stringResource(R.string.no_transactions_yet),
                    actionLabel = stringResource(R.string.add_transaction),
                    onAction = { onAddTransaction() },
                )
            }
        } else {
            items(rangeTransactions.take(5), key = { it.id }) { transaction ->
                TransactionRow(
                    transaction = transaction,
                    category = transaction.categoryId?.let(categoryById::get),
                    onEdit = { editingTransaction = transaction },
                )
            }
        }
        item {
            SectionHeader(
                title = stringResource(R.string.report_preview),
                actionLabel = stringResource(R.string.open_reports),
                onAction = { onOpenReports() },
            )
            Text(
                text = stringResource(
                    R.string.report_preview_body_for_range,
                    MoneyFormatter.formatAmount(expenses),
                    MoneyFormatter.formatAmount(income),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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

@Composable
private fun DashboardRangeSelector(
    selectedType: DashboardRangeType,
    dateRange: DashboardDateRange,
    locale: Locale,
    onTypeSelected: (DashboardRangeType) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.previous_range),
                )
            }
            Text(
                text = dateRange.label(selectedType, locale),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.next_range),
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            DashboardRangeType.entries.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.label()) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DonutMetricCard(
    title: String,
    amountMinor: Long,
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    amountColor: Color = MaterialTheme.colorScheme.onSurface,
    emphasized: Boolean = false,
) {
    val visibleSlices = slices.filter { it.amountMinor > 0 }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Box(contentAlignment = Alignment.Center) {
                DonutChart(
                    slices = visibleSlices,
                    modifier = Modifier.size(if (emphasized) 172.dp else 128.dp),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = MoneyFormatter.formatAmount(amountMinor),
                        style = if (emphasized) {
                            MaterialTheme.typography.headlineSmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        fontWeight = FontWeight.SemiBold,
                        color = amountColor,
                        textAlign = TextAlign.Center,
                    )
                    if (visibleSlices.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_chart_data),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            DonutLegend(slices = visibleSlices.take(4))
        }
    }
}

@Composable
private fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
) {
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.16f, cap = StrokeCap.Butt)
        val inset = stroke.width / 2f
        val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
        val total = slices.sumOf { it.amountMinor }.toFloat()
        if (total <= 0f) {
            drawArc(
                color = emptyColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
            return@Canvas
        }
        var startAngle = -90f
        slices.forEach { slice ->
            val sweep = 360f * (slice.amountMinor / total)
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun DonutLegend(slices: List<DonutSlice>) {
    if (slices.isEmpty()) return
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        slices.forEach { slice ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(slice.color),
                )
                Text(
                    text = slice.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = MoneyFormatter.formatAmount(slice.amountMinor),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

internal enum class DashboardRangeType {
    Day,
    Week,
    Month,
    Year,
}

internal data class DashboardDateRange(
    val startDate: LocalDate,
    val endExclusiveDate: LocalDate,
) {
    companion object {
        fun from(
            type: DashboardRangeType,
            anchorDate: LocalDate,
            locale: Locale,
        ): DashboardDateRange {
            val start = when (type) {
                DashboardRangeType.Day -> anchorDate
                DashboardRangeType.Week -> anchorDate.with(
                    TemporalAdjusters.previousOrSame(firstDayOfWeek(locale)),
                )
                DashboardRangeType.Month -> YearMonth.from(anchorDate).atDay(1)
                DashboardRangeType.Year -> anchorDate.withDayOfYear(1)
            }
            val endExclusive = when (type) {
                DashboardRangeType.Day -> start.plusDays(1)
                DashboardRangeType.Week -> start.plusWeeks(1)
                DashboardRangeType.Month -> start.plusMonths(1)
                DashboardRangeType.Year -> start.plusYears(1)
            }
            return DashboardDateRange(start, endExclusive)
        }

        private fun firstDayOfWeek(locale: Locale): DayOfWeek =
            runCatching { WeekFields.of(locale).firstDayOfWeek }.getOrDefault(DayOfWeek.MONDAY)
    }
}

private data class DonutSlice(
    val label: String,
    val amountMinor: Long,
    val color: Color,
)

private fun DashboardRangeType.move(anchorDate: LocalDate, amount: Long): LocalDate =
    when (this) {
        DashboardRangeType.Day -> anchorDate.plusDays(amount)
        DashboardRangeType.Week -> anchorDate.plusWeeks(amount)
        DashboardRangeType.Month -> anchorDate.plusMonths(amount)
        DashboardRangeType.Year -> anchorDate.plusYears(amount)
    }

@Composable
private fun DashboardRangeType.label(): String =
    when (this) {
        DashboardRangeType.Day -> stringResource(R.string.range_day)
        DashboardRangeType.Week -> stringResource(R.string.range_week)
        DashboardRangeType.Month -> stringResource(R.string.range_month)
        DashboardRangeType.Year -> stringResource(R.string.range_year)
    }

private fun DashboardDateRange.label(type: DashboardRangeType, locale: Locale): String =
    when (type) {
        DashboardRangeType.Day -> startDate.toString()
        DashboardRangeType.Week -> "${startDate} - ${endExclusiveDate.minusDays(1)}"
        DashboardRangeType.Month -> {
            val month = startDate.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
            "$month ${startDate.year}"
        }
        DashboardRangeType.Year -> startDate.year.toString()
    }

private fun List<TransactionEntity>.categorySlices(
    type: TransactionType,
    categoryById: Map<Long, CategoryEntity>,
    uncategorizedLabel: String,
    fallbackColor: Color,
): List<DonutSlice> =
    filter { it.type == type }
        .groupBy { it.categoryId }
        .map { (categoryId, transactions) ->
            val category = categoryId?.let(categoryById::get)
            DonutSlice(
                label = category?.title ?: uncategorizedLabel,
                amountMinor = transactions.sumOf { it.amountMinor },
                color = category?.toColor() ?: fallbackColor,
            )
        }
        .sortedByDescending { it.amountMinor }
