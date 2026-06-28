package com.ozcomingfroo.mybudget.ui

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddTransactionScreen(
    selectedBudgetBookId: Long?,
    categories: List<CategoryEntity>,
    preferences: AppPreferences,
    insertTransaction: suspend (TransactionEntity) -> Long,
    clock: Clock,
    initialTransactionType: TransactionType? = null,
    showCancelButton: Boolean = false,
    onCancel: () -> Unit = {},
    onTransactionSaved: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var transactionType by rememberSaveable(initialTransactionType, preferences.defaultTransactionType) {
        mutableStateOf(
            initialTransactionType ?: when (preferences.defaultTransactionType) {
                DefaultTransactionType.EXPENSE -> TransactionType.EXPENSE
                DefaultTransactionType.INCOME -> TransactionType.INCOME
            },
        )
    }
    val availableCategories = remember(categories, transactionType) {
        categories.filter { category ->
            when (transactionType) {
                TransactionType.EXPENSE -> category.type == CategoryType.EXPENSE || category.type == CategoryType.BOTH
                TransactionType.INCOME -> category.type == CategoryType.INCOME || category.type == CategoryType.BOTH
            }
        }
    }
    val availableCategoryIds = remember(availableCategories) {
        availableCategories.map { it.id }.toSet()
    }
    var selectedCategoryId by rememberSaveable(transactionType) {
        mutableStateOf(availableCategories.firstOrNull()?.id)
    }
    LaunchedEffect(availableCategoryIds) {
        if (selectedCategoryId !in availableCategoryIds) {
            selectedCategoryId = availableCategories.firstOrNull()?.id
        }
    }

    var amountText by rememberSaveable { mutableStateOf("") }
    var titleText by rememberSaveable { mutableStateOf("") }
    var noteText by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now(clock)) }
    var selectedTime by rememberSaveable { mutableStateOf(LocalTime.now(clock).withSecond(0).withNano(0)) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val missingBookMessage = stringResource(R.string.loading_budget_book)
    val invalidAmountMessage = stringResource(R.string.amount_error)
    val missingCategoryMessage = stringResource(R.string.category_error)
    val saveFailedMessage = stringResource(R.string.transaction_save_error)
    val selectedCategoryType = when (transactionType) {
        TransactionType.EXPENSE -> CategoryType.EXPENSE
        TransactionType.INCOME -> CategoryType.INCOME
    }
    val timeText = remember(selectedTime) {
        String.format(Locale.getDefault(), "%02d:%02d", selectedTime.hour, selectedTime.minute)
    }
    val amountTextStyle = TextStyle(
        fontSize = 28.sp,
        textAlign = TextAlign.Center,
    )
    val dateFieldInteractionSource = remember { MutableInteractionSource() }
    val timeFieldInteractionSource = remember { MutableInteractionSource() }
    val isDateFieldPressed by dateFieldInteractionSource.collectIsPressedAsState()
    val isTimeFieldPressed by timeFieldInteractionSource.collectIsPressedAsState()

    LaunchedEffect(isDateFieldPressed) {
        if (isDateFieldPressed) {
            showDatePicker = true
        }
    }
    LaunchedEffect(isTimeFieldPressed) {
        if (isTimeFieldPressed) {
            showTimePicker = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.amount),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextField(
                        value = amountText,
                        onValueChange = { amountText = it.toAmountInputText() },
                        placeholder = {
                            Text(
                                text = "0",
                                modifier = Modifier.fillMaxWidth(),
                                style = amountTextStyle,
                                textAlign = TextAlign.Center,
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = amountTextStyle,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .width(240.dp)
                            .align(Alignment.CenterHorizontally)
                            .testTag("add_transaction_amount"),
                    )
                }
            }
            item {
                CategoryTypeSwitch(
                    selectedType = selectedCategoryType,
                    onTypeSelected = { selectedType ->
                        transactionType = when (selectedType) {
                            CategoryType.EXPENSE -> TransactionType.EXPENSE
                            CategoryType.INCOME -> TransactionType.INCOME
                            CategoryType.BOTH -> transactionType
                        }
                    },
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.category),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (availableCategories.isEmpty()) {
                        EmptyState(text = stringResource(R.string.no_categories_yet))
                    } else {
                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(292.dp)
                                .testTag("add_transaction_category_grid"),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 2.dp),
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
                                        .testTag("add_transaction_category_${category.id}"),
                                )
                            }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = selectedDate.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.date)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                            )
                        },
                        singleLine = true,
                        interactionSource = dateFieldInteractionSource,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_transaction_date"),
                    )
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.time)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                            )
                        },
                        singleLine = true,
                        interactionSource = timeFieldInteractionSource,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_transaction_time"),
                    )
                }
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
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showCancelButton) {
                FilledTonalButton(
                    onClick = onCancel,
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_transaction_cancel"),
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
            Button(
                onClick = {
                    if (isSaving) {
                        return@Button
                    }
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
                    val categoryId = selectedCategoryId
                    if (categoryId == null) {
                        errorText = missingCategoryMessage
                        return@Button
                    }
                    errorText = null
                    isSaving = true
                    scope.launch {
                        try {
                            val now = clock.instant()
                            insertTransaction(
                                TransactionEntity(
                                    budgetBookId = budgetBookId,
                                    categoryId = categoryId,
                                    type = transactionType,
                                    amountMinor = amountMinor,
                                    title = titleText.trim().ifBlank { null },
                                    note = noteText.trim().ifBlank { null },
                                    occurredAt = LocalDateTime.of(selectedDate, selectedTime),
                                    createdAt = now,
                                    updatedAt = now,
                                ),
                            )
                            onTransactionSaved()
                        } catch (cancellation: CancellationException) {
                            isSaving = false
                            throw cancellation
                        } catch (_: Exception) {
                            isSaving = false
                            snackbarHostState.showSnackbar(saveFailedMessage)
                        }
                    }
                },
                enabled = amountText.isNotBlank() && selectedBudgetBookId != null && selectedCategoryId != null && !isSaving,
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_transaction_save"),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(stringResource(R.string.save_transaction))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toDatePickerMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate = it.toDatePickerDate() }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditTransactionSheet(
    transaction: TransactionEntity,
    categories: List<CategoryEntity>,
    clock: Clock,
    onDismiss: () -> Unit,
    onTransactionUpdated: suspend (TransactionEntity) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var transactionType by rememberSaveable(transaction.id) { mutableStateOf(transaction.type) }
    val availableCategories = remember(categories, transactionType) {
        categories.filter { category ->
            when (transactionType) {
                TransactionType.EXPENSE -> category.type == CategoryType.EXPENSE || category.type == CategoryType.BOTH
                TransactionType.INCOME -> category.type == CategoryType.INCOME || category.type == CategoryType.BOTH
            }
        }
    }
    val availableCategoryIds = remember(availableCategories) {
        availableCategories.map { it.id }.toSet()
    }
    var selectedCategoryId by rememberSaveable(transaction.id, transactionType) {
        mutableStateOf(transaction.categoryId ?: availableCategories.firstOrNull()?.id)
    }
    LaunchedEffect(availableCategoryIds) {
        if (selectedCategoryId !in availableCategoryIds) {
            selectedCategoryId = availableCategories.firstOrNull()?.id
        }
    }

    var amountText by rememberSaveable(transaction.id) {
        mutableStateOf(MoneyFormatter.formatAmount(transaction.amountMinor))
    }
    var titleText by rememberSaveable(transaction.id) { mutableStateOf(transaction.title.orEmpty()) }
    var noteText by rememberSaveable(transaction.id) { mutableStateOf(transaction.note.orEmpty()) }
    var selectedDate by rememberSaveable(transaction.id) { mutableStateOf(transaction.occurredAt.toLocalDate()) }
    var selectedTime by rememberSaveable(transaction.id) { mutableStateOf(transaction.occurredAt.toLocalTime()) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val invalidAmountMessage = stringResource(R.string.amount_error)
    val missingCategoryMessage = stringResource(R.string.category_error)
    val updatedMessage = stringResource(R.string.transaction_updated)
    val saveFailedMessage = stringResource(R.string.transaction_save_error)
    val selectedCategoryType = when (transactionType) {
        TransactionType.EXPENSE -> CategoryType.EXPENSE
        TransactionType.INCOME -> CategoryType.INCOME
    }
    val timeText = remember(selectedTime) {
        String.format(Locale.getDefault(), "%02d:%02d", selectedTime.hour, selectedTime.minute)
    }
    val amountTextStyle = TextStyle(
        fontSize = 28.sp,
        textAlign = TextAlign.Center,
    )
    val dateFieldInteractionSource = remember { MutableInteractionSource() }
    val timeFieldInteractionSource = remember { MutableInteractionSource() }
    val isDateFieldPressed by dateFieldInteractionSource.collectIsPressedAsState()
    val isTimeFieldPressed by timeFieldInteractionSource.collectIsPressedAsState()

    LaunchedEffect(isDateFieldPressed) {
        if (isDateFieldPressed) {
            showDatePicker = true
        }
    }
    LaunchedEffect(isTimeFieldPressed) {
        if (isTimeFieldPressed) {
            showTimePicker = true
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.amount),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextField(
                            value = amountText,
                            onValueChange = { amountText = it.toAmountInputText() },
                            placeholder = {
                                Text(
                                    text = "0",
                                    modifier = Modifier.fillMaxWidth(),
                                    style = amountTextStyle,
                                    textAlign = TextAlign.Center,
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            textStyle = amountTextStyle,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                            ),
                            modifier = Modifier
                                .width(240.dp)
                                .align(Alignment.CenterHorizontally)
                                .testTag("add_transaction_amount"),
                        )
                    }
                }
                item {
                    CategoryTypeSwitch(
                        selectedType = selectedCategoryType,
                        onTypeSelected = { selectedType ->
                            transactionType = when (selectedType) {
                                CategoryType.EXPENSE -> TransactionType.EXPENSE
                                CategoryType.INCOME -> TransactionType.INCOME
                                CategoryType.BOTH -> transactionType
                            }
                        },
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = stringResource(R.string.category),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (availableCategories.isEmpty()) {
                            EmptyState(text = stringResource(R.string.no_categories_yet))
                        } else {
                            LazyHorizontalGrid(
                                rows = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(292.dp)
                                    .testTag("add_transaction_category_grid"),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 2.dp),
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
                                            .testTag("add_transaction_category_${category.id}"),
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = selectedDate.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.date)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                )
                            },
                            singleLine = true,
                            interactionSource = dateFieldInteractionSource,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_transaction_date"),
                        )
                        OutlinedTextField(
                            value = timeText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.time)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.AccessTime,
                                    contentDescription = null,
                                )
                            },
                            singleLine = true,
                            interactionSource = timeFieldInteractionSource,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_transaction_time"),
                        )
                    }
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
            }

            Button(
                onClick = {
                    if (isSaving) {
                        return@Button
                    }
                    val amountMinor = try {
                        MoneyFormatter.parseAmountMinor(amountText)
                    } catch (_: IllegalArgumentException) {
                        errorText = invalidAmountMessage
                        return@Button
                    }
                    val categoryId = selectedCategoryId
                    if (categoryId == null) {
                        errorText = missingCategoryMessage
                        return@Button
                    }
                    errorText = null
                    isSaving = true
                    scope.launch {
                        try {
                            onTransactionUpdated(
                                transaction.copy(
                                    categoryId = categoryId,
                                    type = transactionType,
                                    amountMinor = amountMinor,
                                    title = titleText.trim().ifBlank { null },
                                    note = noteText.trim().ifBlank { null },
                                    occurredAt = LocalDateTime.of(selectedDate, selectedTime),
                                    updatedAt = clock.instant(),
                                ),
                            )
                            onDismiss()
                            snackbarHostState.showSnackbar(updatedMessage)
                        } catch (cancellation: CancellationException) {
                            isSaving = false
                            throw cancellation
                        } catch (_: Exception) {
                            isSaving = false
                            snackbarHostState.showSnackbar(saveFailedMessage)
                        }
                    }
                },
                enabled = amountText.isNotBlank() && selectedCategoryId != null && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("add_transaction_save"),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(stringResource(R.string.update_transaction))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toDatePickerMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate = it.toDatePickerDate() }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }
}

private fun LocalDate.toDatePickerMillis(): Long =
    atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toDatePickerDate(): LocalDate =
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
