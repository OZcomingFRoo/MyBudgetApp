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
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
internal fun CategoriesScreen(
    selectedBudgetBookId: Long?,
    categories: List<CategoryEntity>,
    categoryRepository: CategoryRepository,
    snackbarHostState: SnackbarHostState,
) {
    var selectedType by rememberSaveable { mutableStateOf(CategoryType.EXPENSE) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var editorInitialType by rememberSaveable { mutableStateOf(CategoryType.EXPENSE) }
    var showEditor by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val visibleCategories = categories.filter { it.type == selectedType || it.type == CategoryType.BOTH }
    val createdMessage = stringResource(R.string.category_created)
    val updatedMessage = stringResource(R.string.category_updated)
    val deletedMessage = stringResource(R.string.category_deleted)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.categories_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
        CategoryTypeSwitch(
            selectedType = selectedType,
            onTypeSelected = { selectedType = it },
            modifier = Modifier.padding(top = 12.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            gridItems(visibleCategories, key = { it.id }) { category ->
                CategoryGridTile(
                    category = category,
                    onClick = {
                        editingCategory = category
                        editorInitialType = if (category.type == CategoryType.INCOME) {
                            CategoryType.INCOME
                        } else {
                            CategoryType.EXPENSE
                        }
                        showEditor = true
                    },
                )
            }
            item(key = "create") {
                CreateCategoryTile(
                    enabled = selectedBudgetBookId != null,
                    onClick = {
                        editingCategory = null
                        editorInitialType = selectedType
                        showEditor = true
                    },
                )
            }
            if (visibleCategories.isEmpty() && selectedBudgetBookId == null) {
                item(key = "loading") {
                    EmptyState(text = stringResource(R.string.loading_budget_book))
                }
            }
        }
    }

    if (showEditor) {
        CategoryEditorSheet(
            category = editingCategory,
            initialType = editorInitialType,
            onSave = { form ->
                scope.launch {
                    val category = editingCategory
                    if (category == null) {
                        categoryRepository.createCategory(
                            budgetBookId = selectedBudgetBookId ?: return@launch,
                            title = form.title.trim(),
                            type = form.type,
                            iconName = form.iconName,
                            color = form.color,
                            sortOrder = (categories.maxOfOrNull { it.sortOrder } ?: -1) + 1,
                        )
                        showEditor = false
                        snackbarHostState.showSnackbar(createdMessage)
                    } else {
                        categoryRepository.update(
                            category.copy(
                                title = form.title.trim(),
                                type = form.type,
                                iconName = form.iconName,
                                color = form.color,
                            ),
                        )
                        showEditor = false
                        snackbarHostState.showSnackbar(updatedMessage)
                    }
                }
            },
            onDelete = { category ->
                scope.launch {
                    categoryRepository.archive(category.id)
                    showEditor = false
                    snackbarHostState.showSnackbar(deletedMessage)
                }
            },
            onDismiss = { showEditor = false },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun CategoryTypeSwitch(
    selectedType: CategoryType,
    onTypeSelected: (CategoryType) -> Unit,
    modifier: Modifier = Modifier,
    labels: CategoryTypeSwitchLabels = CategoryTypeSwitchLabels(
        expense = stringResource(R.string.expense),
        income = stringResource(R.string.income),
    ),
) {
    val types = listOf(CategoryType.EXPENSE, CategoryType.INCOME)
    val contentLayoutDirection = LocalLayoutDirection.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        SingleChoiceSegmentedButtonRow(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            types.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                    icon = {},
                    modifier = Modifier.weight(1f),
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides contentLayoutDirection) {
                        Text(
                            text = labels.labelFor(type),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

internal data class CategoryTypeSwitchLabels(
    val expense: String,
    val income: String,
)

private fun CategoryTypeSwitchLabels.labelFor(type: CategoryType): String = when (type) {
    CategoryType.EXPENSE -> expense
    CategoryType.INCOME -> income
    CategoryType.BOTH -> expense
}

@Composable
private fun CategoryGridTile(
    category: CategoryEntity,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CategoryIconCircle(
                iconName = category.iconName,
                color = category.toColor(),
                contentDescription = stringResource(R.string.category_icon_content_description, category.title),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun CreateCategoryTile(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.create_category),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun CategoryIconCircle(
    iconName: String,
    color: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = iconName.toCategoryIcon(),
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(34.dp),
        )
    }
}

private data class CategoryIconOption(
    val iconName: String,
    val imageVector: ImageVector,
    val contentDescription: String,
)

private val CategoryIconOptions = listOf(
    CategoryIconOption("shopping_cart", Icons.Filled.ShoppingCart, "Shopping cart"),
    CategoryIconOption("restaurant", Icons.Filled.Restaurant, "Restaurant"),
    CategoryIconOption("home", Icons.Filled.Home, "Home"),
    CategoryIconOption("receipt", Icons.Filled.Receipt, "Receipt"),
    CategoryIconOption("phone_android", Icons.Filled.PhoneAndroid, "Phone"),
    CategoryIconOption("directions_car", Icons.Filled.DirectionsCar, "Car"),
    CategoryIconOption("medical_services", Icons.Filled.LocalHospital, "Health"),
    CategoryIconOption("shopping_bag", Icons.Filled.ShoppingBag, "Shopping bag"),
    CategoryIconOption("movie", Icons.Filled.Movie, "Movie"),
    CategoryIconOption("payments", Icons.Filled.Payments, "Payments"),
    CategoryIconOption("work", Icons.Filled.Work, "Work"),
    CategoryIconOption("attach_money", Icons.Filled.AttachMoney, "Money"),
    CategoryIconOption("undo", Icons.AutoMirrored.Filled.Undo, "Refund"),
    CategoryIconOption("card_giftcard", Icons.Filled.CardGiftcard, "Gift"),
    CategoryIconOption("sports", Icons.Filled.SportsSoccer, "Sports"),
    CategoryIconOption("education", Icons.Filled.School, "Education"),
    CategoryIconOption("tech", Icons.Filled.SportsEsports, "Tech"),
    CategoryIconOption("category", Icons.Filled.Category, "Category"),
    CategoryIconOption("other", Icons.AutoMirrored.Filled.Help, "Other"),
)

private val CategoryColorOptions = listOf(
    "#2E7D32",
    "#C2410C",
    "#334155",
    "#0891B2",
    "#2563EB",
    "#7C3AED",
    "#DC2626",
    "#4F46E5",
    "#DB2777",
    "#15803D",
    "#0F766E",
    "#0369A1",
    "#65A30D",
    "#BE185D",
    "#EA580C",
    "#64748B",
    "#E7B84B",
    "#EC4899",
)

private fun String.toCategoryIcon(): ImageVector =
    CategoryIconOptions.firstOrNull { it.iconName == this }?.imageVector ?: Icons.Filled.Category

private fun String.toColor(): Color =
    runCatching { Color(android.graphics.Color.parseColor(this)) }
        .getOrDefault(Color.Gray)
