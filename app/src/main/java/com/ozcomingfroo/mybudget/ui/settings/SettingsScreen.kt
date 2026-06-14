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
internal fun SettingsScreen(
    preferences: AppPreferences,
    currentBudgetBook: BudgetBookEntity?,
    appPreferencesRepository: AppPreferencesRepository,
    budgetBookRepository: BudgetBookRepository,
) {
    val scope = rememberCoroutineScope()
    val versionName = rememberAppVersionName() ?: stringResource(R.string.version_unavailable)
    var budgetBookName by rememberSaveable(currentBudgetBook?.id) {
        mutableStateOf(currentBudgetBook?.title.orEmpty())
    }
    var hasSavedBudgetBookName by rememberSaveable(currentBudgetBook?.id) {
        mutableStateOf(false)
    }
    val trimmedBudgetBookName = budgetBookName.trim()
    val canSaveBudgetBookName = currentBudgetBook != null &&
        trimmedBudgetBookName.isNotBlank() &&
        trimmedBudgetBookName != currentBudgetBook.title

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SettingsSection(
                title = stringResource(R.string.settings_budget_book),
                supportingText = stringResource(R.string.budget_book_name_helper),
            ) {
                OutlinedTextField(
                    value = budgetBookName,
                    onValueChange = {
                        budgetBookName = it
                        hasSavedBudgetBookName = false
                    },
                    label = { Text(stringResource(R.string.budget_book_name)) },
                    supportingText = {
                        Text(
                            when {
                                currentBudgetBook == null -> stringResource(R.string.preparing_budget_book)
                                trimmedBudgetBookName.isBlank() -> stringResource(R.string.budget_book_name_required)
                                hasSavedBudgetBookName -> stringResource(R.string.budget_book_name_saved)
                                else -> stringResource(R.string.budget_book_name_settings_helper)
                            },
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                )
                Button(
                    onClick = {
                        currentBudgetBook?.id?.let { budgetBookId ->
                            scope.launch {
                                budgetBookRepository.renameBudgetBook(budgetBookId, budgetBookName)
                                budgetBookName = trimmedBudgetBookName
                                hasSavedBudgetBookName = true
                            }
                        }
                    },
                    enabled = canSaveBudgetBookName,
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_appearance)) {
                ThemeSelector(
                    selectedThemeMode = preferences.themeMode,
                    onThemeModeSelected = { mode ->
                        scope.launch { appPreferencesRepository.setThemeMode(mode) }
                    },
                )
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_language)) {
                LanguageSelector(
                    selectedLanguageMode = preferences.languageMode,
                    onLanguageModeSelected = { mode ->
                        scope.launch { appPreferencesRepository.setLanguageMode(mode) }
                    },
                )
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
private fun ThemeSelector(
    selectedThemeMode: AppThemeMode,
    onThemeModeSelected: (AppThemeMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ThemeTile(
            mode = AppThemeMode.DEFAULT,
            previewBackgroundColor = BudgetSurface,
            activeColor = BudgetGreen,
            labelColor = BudgetGreenDark,
            selected = selectedThemeMode == AppThemeMode.DEFAULT,
            onClick = { onThemeModeSelected(AppThemeMode.DEFAULT) },
            modifier = Modifier.weight(1f),
        )
        ThemeTile(
            mode = AppThemeMode.NIGHT,
            previewBackgroundColor = BudgetBlack,
            activeColor = BudgetWarmYellow,
            labelColor = BudgetWarmYellow,
            selected = selectedThemeMode == AppThemeMode.NIGHT,
            onClick = { onThemeModeSelected(AppThemeMode.NIGHT) },
            modifier = Modifier.weight(1f),
        )
    }
    RadioSettingRow(
        label = AppThemeMode.SYSTEM.label(),
        selected = selectedThemeMode == AppThemeMode.SYSTEM,
        onClick = { onThemeModeSelected(AppThemeMode.SYSTEM) },
    )
}

@Composable
private fun ThemeTile(
    mode: AppThemeMode,
    previewBackgroundColor: Color,
    activeColor: Color,
    labelColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tileShape = RoundedCornerShape(8.dp)
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = modifier
            .heightIn(min = 120.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        shape = tileShape,
        colors = CardDefaults.cardColors(containerColor = previewBackgroundColor),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentAlignment = Alignment.Center,
            ) {
                RadioButton(
                    selected = selected,
                    onClick = null,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(32.dp)
                    .clip(CircleShape)
                    .background(activeColor),
            )
            Text(
                text = mode.label(),
                style = MaterialTheme.typography.labelLarge,
                color = labelColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LanguageSelector(
    selectedLanguageMode: AppLanguageMode,
    onLanguageModeSelected: (AppLanguageMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LanguageTile(
            mode = AppLanguageMode.EN_US,
            flagResId = R.drawable.flag_us,
            selected = selectedLanguageMode == AppLanguageMode.EN_US,
            onClick = { onLanguageModeSelected(AppLanguageMode.EN_US) },
            modifier = Modifier.weight(1f),
        )
        LanguageTile(
            mode = AppLanguageMode.HE,
            flagResId = R.drawable.flag_israel,
            selected = selectedLanguageMode == AppLanguageMode.HE,
            onClick = { onLanguageModeSelected(AppLanguageMode.HE) },
            modifier = Modifier.weight(1f),
        )
    }
    RadioSettingRow(
        label = AppLanguageMode.SYSTEM.label(),
        selected = selectedLanguageMode == AppLanguageMode.SYSTEM,
        onClick = { onLanguageModeSelected(AppLanguageMode.SYSTEM) },
    )
}

@Composable
private fun LanguageTile(
    mode: AppLanguageMode,
    flagResId: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tileShape = RoundedCornerShape(8.dp)
    val flagShape = RoundedCornerShape(12.dp)
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = modifier
            .heightIn(min = 120.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        shape = tileShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(flagResId),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .width(72.dp)
                        .height(48.dp)
                        .clip(flagShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, flagShape),
                )
                RadioButton(
                    selected = selected,
                    onClick = null,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
            Text(
                text = mode.label(),
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
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

private fun Context.packageVersionName(): String? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0).versionName
    }
}.getOrNull()?.takeIf { it.isNotBlank() }
