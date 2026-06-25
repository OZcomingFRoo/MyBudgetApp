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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.core.money.MoneyFormatter
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import com.ozcomingfroo.mybudget.data.repository.TransactionRepository
import com.ozcomingfroo.mybudget.ui.theme.ExpenseRed
import com.ozcomingfroo.mybudget.ui.theme.IncomeGreen
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun ReportsScreen(
    selectedBudgetBookId: Long?,
    categories: List<CategoryEntity>,
    transactionRepository: TransactionRepository,
    clock: Clock,
    onAddTransaction: () -> Unit,
) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    var rangeType by rememberSaveable { mutableStateOf(ReportsRangeType.Month) }
    var anchorEpochDay by rememberSaveable { mutableLongStateOf(LocalDate.now(clock).toEpochDay()) }
    var customStartEpochDay by rememberSaveable {
        mutableLongStateOf(LocalDate.now(clock).withDayOfMonth(1).toEpochDay())
    }
    var customEndEpochDay by rememberSaveable { mutableLongStateOf(LocalDate.now(clock).toEpochDay()) }
    val anchorDate = LocalDate.ofEpochDay(anchorEpochDay)
    val customStartDate = LocalDate.ofEpochDay(customStartEpochDay)
    val customEndDate = LocalDate.ofEpochDay(customEndEpochDay)
    val customRange = remember(customStartDate, customEndDate) {
        ReportsDateRange.fromCustomDates(customStartDate, customEndDate)
    }
    val dateRange = remember(rangeType, anchorDate, customRange, locale) {
        if (rangeType == ReportsRangeType.Custom) {
            customRange
        } else {
            ReportsDateRange.from(rangeType, anchorDate, locale)
        }
    }
    val rangeTransactions by remember(selectedBudgetBookId, dateRange) {
        val range = dateRange
        if (selectedBudgetBookId != null && range != null) {
            transactionRepository.observeForDateRange(
                budgetBookId = selectedBudgetBookId,
                startDate = range.startDate,
                endExclusiveDate = range.endExclusiveDate,
            )
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())
    val categoryById = remember(categories) { categories.associateBy { it.id } }
    val uncategorizedLabel = stringResource(R.string.uncategorized)
    val otherCategoryLabel = stringResource(R.string.other_category)
    val buckets = remember(rangeTransactions, dateRange, rangeType, locale) {
        dateRange?.buildTrendBuckets(rangeTransactions, rangeType, locale).orEmpty()
    }
    val categoryTotals = remember(rangeTransactions, categoryById, uncategorizedLabel) {
        rangeTransactions.expenseCategoryTotals(
            categoryById = categoryById,
            uncategorizedLabel = uncategorizedLabel,
            fallbackColor = Color.Gray,
        )
    }
    val groupedCategoryTotals = remember(categoryTotals, otherCategoryLabel) {
        categoryTotals.groupSmallCategories(otherLabel = otherCategoryLabel)
    }
    val totalIncome = remember(rangeTransactions) { rangeTransactions.total(TransactionType.INCOME) }
    val totalExpenses = remember(rangeTransactions) { rangeTransactions.total(TransactionType.EXPENSE) }
    val hasReportData = rangeTransactions.isNotEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ReportsRangeSelector(
                selectedType = rangeType,
                dateRange = dateRange,
                locale = locale,
                customStartDate = customStartDate,
                customEndDate = customEndDate,
                onTypeSelected = {
                    rangeType = it
                    if (it != ReportsRangeType.Custom) {
                        anchorEpochDay = LocalDate.now(clock).toEpochDay()
                    }
                },
                onPrevious = { anchorEpochDay = rangeType.move(anchorDate, -1).toEpochDay() },
                onNext = { anchorEpochDay = rangeType.move(anchorDate, 1).toEpochDay() },
                onCustomStartChange = { selectedDate ->
                    customStartEpochDay = selectedDate.toEpochDay()
                    if (customEndDate.isBefore(selectedDate)) {
                        customEndEpochDay = selectedDate.toEpochDay()
                    }
                },
                onCustomEndChange = { selectedDate ->
                    customEndEpochDay = selectedDate.toEpochDay()
                    if (customStartDate.isAfter(selectedDate)) {
                        customStartEpochDay = selectedDate.toEpochDay()
                    }
                },
            )
        }

        if (dateRange == null) {
            item { EmptyState(text = stringResource(R.string.reports_custom_range_error)) }
        } else if (!hasReportData) {
            item {
                EmptyState(
                    text = stringResource(R.string.no_report_data),
                    actionLabel = stringResource(R.string.add_transaction),
                    onAction = onAddTransaction,
                )
            }
        } else {
            item {
                ReportsTotalsRow(income = totalIncome, expenses = totalExpenses)
            }
            item {
                ChartSection(title = stringResource(R.string.income_vs_expenses)) {
                    IncomeExpenseTrendChart(buckets = buckets)
                    ChartLegend(
                        items = listOf(
                            LegendItem(stringResource(R.string.income), IncomeGreen),
                            LegendItem(stringResource(R.string.expenses), ExpenseRed),
                        ),
                    )
                }
            }
            item {
                ChartSection(title = stringResource(R.string.spending_breakdown)) {
                    SpendingBreakdownChart(
                        categories = groupedCategoryTotals,
                        totalExpenses = totalExpenses,
                    )
                }
            }
            item {
                ChartSection(title = stringResource(R.string.top_categories)) {
                    TopCategoryBars(categories = categoryTotals.take(5))
                }
            }
        }
    }
}

@Composable
private fun ReportsTotalsRow(
    income: Long,
    expenses: Long,
) {
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

@Composable
private fun ReportsRangeSelector(
    selectedType: ReportsRangeType,
    dateRange: ReportsDateRange?,
    locale: Locale,
    customStartDate: LocalDate,
    customEndDate: LocalDate,
    onTypeSelected: (ReportsRangeType) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCustomStartChange: (LocalDate) -> Unit,
    onCustomEndChange: (LocalDate) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onPrevious,
                enabled = selectedType != ReportsRangeType.Custom,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.previous_range),
                )
            }
            Text(
                text = dateRange?.label(selectedType, locale) ?: stringResource(R.string.date_range),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onNext,
                enabled = selectedType != ReportsRangeType.Custom,
            ) {
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
            ReportsRangeType.entries.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.label()) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (selectedType == ReportsRangeType.Custom) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportsDateButton(
                    label = stringResource(R.string.start_date),
                    date = customStartDate,
                    onDateSelected = onCustomStartChange,
                    modifier = Modifier.weight(1f),
                )
                ReportsDateButton(
                    label = stringResource(R.string.end_date),
                    date = customEndDate,
                    onDateSelected = onCustomEndChange,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportsDateButton(
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

@Composable
private fun ChartSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun IncomeExpenseTrendChart(buckets: List<TrendBucket>) {
    val maxAmount = buckets.maxOfOrNull { maxOf(it.incomeMinor, it.expenseMinor) } ?: 0L
    if (buckets.isEmpty() || maxAmount == 0L) {
        EmptyState(text = stringResource(R.string.no_chart_data))
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 168.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        buckets.forEach { bucket ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.height(124.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    TrendBar(amountMinor = bucket.incomeMinor, maxAmount = maxAmount, color = IncomeGreen)
                    TrendBar(amountMinor = bucket.expenseMinor, maxAmount = maxAmount, color = ExpenseRed)
                }
                Text(
                    text = bucket.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TrendBar(
    amountMinor: Long,
    maxAmount: Long,
    color: Color,
) {
    val fraction = if (maxAmount == 0L) 0f else amountMinor.toFloat() / maxAmount.toFloat()
    Box(
        modifier = Modifier
            .width(10.dp)
            .height(124.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((116 * fraction.coerceIn(0f, 1f)).dp.coerceAtLeast(4.dp))
                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                .background(color),
        )
    }
}

@Composable
private fun SpendingBreakdownChart(
    categories: List<CategoryReportItem>,
    totalExpenses: Long,
) {
    val visibleCategories = categories.filter { it.amountMinor > 0 }
    if (visibleCategories.isEmpty()) {
        EmptyState(text = stringResource(R.string.no_chart_data))
        return
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            DonutChart(
                items = visibleCategories,
                modifier = Modifier.size(180.dp),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.total_spent),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = MoneyFormatter.formatAmount(totalExpenses),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = ExpenseRed,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            visibleCategories.forEach { item ->
                CategoryBreakdownRow(item = item, totalExpenses = totalExpenses)
            }
        }
    }
}

@Composable
private fun DonutChart(
    items: List<CategoryReportItem>,
    modifier: Modifier = Modifier,
) {
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.18f, cap = StrokeCap.Butt)
        val inset = stroke.width / 2f
        val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
        val total = items.sumOf { it.amountMinor }.toFloat()
        if (total <= 0f) {
            drawArc(
                color = emptyColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
            return@Canvas
        }
        var startAngle = -90f
        items.forEach { item ->
            val sweep = 360f * (item.amountMinor / total)
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun CategoryBreakdownRow(
    item: CategoryReportItem,
    totalExpenses: Long,
) {
    val percent = item.percentOf(totalExpenses)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CategoryIconCircle(
            iconName = item.iconName,
            color = item.color,
            contentDescription = stringResource(R.string.category_icon_content_description, item.label),
            size = 32.dp,
            iconSize = 18.dp,
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        Text(
            text = stringResource(R.string.percent_value, percent),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = MoneyFormatter.formatAmount(item.amountMinor),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun TopCategoryBars(categories: List<CategoryReportItem>) {
    val maxAmount = categories.maxOfOrNull { it.amountMinor } ?: 0L
    if (categories.isEmpty() || maxAmount == 0L) {
        EmptyState(text = stringResource(R.string.no_chart_data))
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        categories.forEach { item ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CategoryIconCircle(
                        iconName = item.iconName,
                        color = item.color,
                        contentDescription = stringResource(R.string.category_icon_content_description, item.label),
                        size = 32.dp,
                        iconSize = 18.dp,
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    Text(
                        text = MoneyFormatter.formatAmount(item.amountMinor),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((item.amountMinor.toFloat() / maxAmount.toFloat()).coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(item.color),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartLegend(items: List<LegendItem>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryDot(color = item.color)
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

internal enum class ReportsRangeType {
    Week,
    Month,
    Year,
    Custom,
}

internal data class ReportsDateRange(
    val startDate: LocalDate,
    val endExclusiveDate: LocalDate,
) {
    companion object {
        fun from(
            type: ReportsRangeType,
            anchorDate: LocalDate,
            locale: Locale,
        ): ReportsDateRange {
            val start = when (type) {
                ReportsRangeType.Week -> anchorDate.with(
                    TemporalAdjusters.previousOrSame(firstDayOfWeek(locale)),
                )
                ReportsRangeType.Month -> YearMonth.from(anchorDate).atDay(1)
                ReportsRangeType.Year -> anchorDate.withDayOfYear(1)
                ReportsRangeType.Custom -> anchorDate
            }
            val endExclusive = when (type) {
                ReportsRangeType.Week -> start.plusWeeks(1)
                ReportsRangeType.Month -> start.plusMonths(1)
                ReportsRangeType.Year -> start.plusYears(1)
                ReportsRangeType.Custom -> start.plusDays(1)
            }
            return ReportsDateRange(start, endExclusive)
        }

        fun fromCustomDates(startDate: LocalDate, endDate: LocalDate): ReportsDateRange? =
            if (endDate.isBefore(startDate)) null else ReportsDateRange(startDate, endDate.plusDays(1))

        private fun firstDayOfWeek(locale: Locale): DayOfWeek =
            runCatching { WeekFields.of(locale).firstDayOfWeek }.getOrDefault(DayOfWeek.MONDAY)
    }
}

internal data class TrendBucket(
    val label: String,
    val incomeMinor: Long,
    val expenseMinor: Long,
)

private data class CategoryReportItem(
    val label: String,
    val amountMinor: Long,
    val color: Color,
    val iconName: String,
)

private data class LegendItem(
    val label: String,
    val color: Color,
)

private fun ReportsRangeType.move(anchorDate: LocalDate, amount: Long): LocalDate =
    when (this) {
        ReportsRangeType.Week -> anchorDate.plusWeeks(amount)
        ReportsRangeType.Month -> anchorDate.plusMonths(amount)
        ReportsRangeType.Year -> anchorDate.plusYears(amount)
        ReportsRangeType.Custom -> anchorDate
    }

@Composable
private fun ReportsRangeType.label(): String =
    when (this) {
        ReportsRangeType.Week -> stringResource(R.string.range_week)
        ReportsRangeType.Month -> stringResource(R.string.range_month)
        ReportsRangeType.Year -> stringResource(R.string.range_year)
        ReportsRangeType.Custom -> stringResource(R.string.range_custom)
    }

private fun ReportsDateRange.label(type: ReportsRangeType, locale: Locale): String =
    when (type) {
        ReportsRangeType.Week -> "${startDate} - ${endExclusiveDate.minusDays(1)}"
        ReportsRangeType.Month -> {
            val month = startDate.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
            "$month ${startDate.year}"
        }
        ReportsRangeType.Year -> startDate.year.toString()
        ReportsRangeType.Custom -> "${startDate} - ${endExclusiveDate.minusDays(1)}"
    }

internal fun ReportsDateRange.buildTrendBuckets(
    transactions: List<TransactionEntity>,
    type: ReportsRangeType,
    locale: Locale,
): List<TrendBucket> {
    val ranges = when (type) {
        ReportsRangeType.Week -> dailyRanges()
        ReportsRangeType.Month -> weeklyRanges(locale)
        ReportsRangeType.Year -> monthlyRanges()
        ReportsRangeType.Custom -> customRanges(locale)
    }
    val totals = ranges.map { MutableTrendTotals() }
    transactions.forEach { transaction ->
        val date = transaction.occurredAt.toLocalDate()
        val bucketIndex = ranges.indexOfFirst { range ->
            !date.isBefore(range.startDate) && date.isBefore(range.endExclusiveDate)
        }
        if (bucketIndex != -1) {
            when (transaction.type) {
                TransactionType.INCOME -> totals[bucketIndex].incomeMinor += transaction.amountMinor
                TransactionType.EXPENSE -> totals[bucketIndex].expenseMinor += transaction.amountMinor
            }
        }
    }
    return ranges.mapIndexed { index, bucketRange ->
        val total = totals[index]
        TrendBucket(
            label = bucketRange.shortLabel(type, locale),
            incomeMinor = total.incomeMinor,
            expenseMinor = total.expenseMinor,
        )
    }
}

private data class MutableTrendTotals(
    var incomeMinor: Long = 0,
    var expenseMinor: Long = 0,
)

private fun ReportsDateRange.dailyRanges(): List<ReportsDateRange> =
    generateSequence(startDate) { it.plusDays(1) }
        .takeWhile { it.isBefore(endExclusiveDate) }
        .map { ReportsDateRange(it, it.plusDays(1)) }
        .toList()

private fun ReportsDateRange.weeklyRanges(locale: Locale): List<ReportsDateRange> {
    val firstDay = runCatching { WeekFields.of(locale).firstDayOfWeek }.getOrDefault(DayOfWeek.MONDAY)
    val firstStart = startDate.with(TemporalAdjusters.previousOrSame(firstDay))
    return generateSequence(firstStart) { it.plusWeeks(1) }
        .takeWhile { it.isBefore(endExclusiveDate) }
        .map { ReportsDateRange(maxOf(it, startDate), minOf(it.plusWeeks(1), endExclusiveDate)) }
        .toList()
}

private fun ReportsDateRange.monthlyRanges(): List<ReportsDateRange> =
    generateSequence(YearMonth.from(startDate)) { it.plusMonths(1) }
        .takeWhile { it.atDay(1).isBefore(endExclusiveDate) }
        .map { ReportsDateRange(it.atDay(1), minOf(it.plusMonths(1).atDay(1), endExclusiveDate)) }
        .toList()

private fun ReportsDateRange.customRanges(locale: Locale): List<ReportsDateRange> {
    val days = ChronoUnit.DAYS.between(startDate, endExclusiveDate)
    return when {
        days <= 14 -> dailyRanges()
        days <= 93 -> weeklyRanges(locale)
        else -> monthlyRanges()
    }
}

private fun ReportsDateRange.shortLabel(type: ReportsRangeType, locale: Locale): String =
    when (type) {
        ReportsRangeType.Week -> startDate.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
        ReportsRangeType.Month -> "${startDate.monthValue}/${startDate.dayOfMonth}"
        ReportsRangeType.Year -> startDate.month.getDisplayName(TextStyle.SHORT, locale)
        ReportsRangeType.Custom -> {
            if (ChronoUnit.DAYS.between(startDate, endExclusiveDate) <= 1) {
                "${startDate.monthValue}/${startDate.dayOfMonth}"
            } else {
                "${startDate.monthValue}/${startDate.dayOfMonth}"
            }
        }
    }

private fun List<TransactionEntity>.expenseCategoryTotals(
    categoryById: Map<Long, CategoryEntity>,
    uncategorizedLabel: String,
    fallbackColor: Color,
): List<CategoryReportItem> =
    filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.categoryId }
        .map { (categoryId, transactions) ->
            val category = categoryId?.let(categoryById::get)
            CategoryReportItem(
                label = category?.title ?: uncategorizedLabel,
                amountMinor = transactions.sumOf { it.amountMinor },
                color = category?.toColor() ?: fallbackColor,
                iconName = category?.iconName ?: "category",
            )
        }
        .sortedByDescending { it.amountMinor }

private fun List<CategoryReportItem>.groupSmallCategories(otherLabel: String): List<CategoryReportItem> {
    if (size <= 5) return this
    val leading = take(4)
    val other = drop(4)
    return leading + CategoryReportItem(
        label = otherLabel,
        amountMinor = other.sumOf { it.amountMinor },
        color = Color.Gray,
        iconName = "other",
    )
}

private fun CategoryReportItem.percentOf(totalMinor: Long): Int =
    if (totalMinor <= 0L) 0 else ((amountMinor * 100) / totalMinor).toInt()

private fun LocalDate.toPickerMillis(): Long =
    atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toPickerDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
