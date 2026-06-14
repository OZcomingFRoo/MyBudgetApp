package com.example.mybudget.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.mybudget.R
import com.example.mybudget.data.preferences.AppLanguageMode
import com.example.mybudget.data.preferences.AppPreferences
import com.example.mybudget.data.preferences.AppPreferencesRepository
import com.example.mybudget.data.preferences.AppThemeMode
import com.example.mybudget.data.preferences.DefaultTransactionType
import com.example.mybudget.data.repository.BudgetBookRepository
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    preferences: AppPreferences,
    selectedBudgetBookId: Long?,
    appPreferencesRepository: AppPreferencesRepository,
    budgetBookRepository: BudgetBookRepository,
) {
    val scope = rememberCoroutineScope()
    var budgetBookName by rememberSaveable { mutableStateOf("Personal") }
    val completeOnboarding: () -> Unit = {
        scope.launch { appPreferencesRepository.setHasCompletedOnboarding(true) }
    }
    val startBudgeting: () -> Unit = {
        scope.launch {
            selectedBudgetBookId?.let { budgetBookId ->
                budgetBookRepository.renameBudgetBook(budgetBookId, budgetBookName)
            }
            appPreferencesRepository.setHasCompletedOnboarding(true)
        }
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.onboarding_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.onboarding_body),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                OnboardingSection(title = stringResource(R.string.onboarding_budget_book_title)) {
                    OutlinedTextField(
                        value = budgetBookName,
                        onValueChange = { budgetBookName = it },
                        label = { Text(stringResource(R.string.budget_book_name)) },
                        supportingText = {
                            Text(
                                if (selectedBudgetBookId == null) {
                                    stringResource(R.string.preparing_budget_book)
                                } else {
                                    stringResource(R.string.budget_book_name_helper)
                                },
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    )
                }
            }
            item {
                OnboardingSection(title = stringResource(R.string.onboarding_privacy_title)) {
                    OnboardingInfoText(text = stringResource(R.string.onboarding_privacy_body))
                }
            }
            item {
                OnboardingSection(title = stringResource(R.string.settings_transaction_defaults)) {
                    DefaultTransactionType.entries.forEach { type ->
                        OnboardingRadioRow(
                            label = type.label(),
                            selected = preferences.defaultTransactionType == type,
                            onClick = {
                                scope.launch { appPreferencesRepository.setDefaultTransactionType(type) }
                            },
                        )
                    }
                }
            }
            item {
                OnboardingSection(title = stringResource(R.string.settings_appearance)) {
                    AppThemeMode.entries.forEach { mode ->
                        OnboardingRadioRow(
                            label = mode.label(),
                            selected = preferences.themeMode == mode,
                            onClick = { scope.launch { appPreferencesRepository.setThemeMode(mode) } },
                        )
                    }
                }
            }
            item {
                OnboardingSection(title = stringResource(R.string.settings_language)) {
                    AppLanguageMode.entries.forEach { mode ->
                        OnboardingRadioRow(
                            label = mode.label(),
                            selected = preferences.languageMode == mode,
                            onClick = { scope.launch { appPreferencesRepository.setLanguageMode(mode) } },
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = completeOnboarding,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.skip))
                    }
                    Button(
                        onClick = startBudgeting,
                        enabled = selectedBudgetBookId != null,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.start_budgeting))
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingSection(
    title: String,
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
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun OnboardingInfoText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
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
private fun OnboardingRadioRow(
    label: String,
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DefaultTransactionType.label(): String = when (this) {
    DefaultTransactionType.EXPENSE -> stringResource(R.string.expense)
    DefaultTransactionType.INCOME -> stringResource(R.string.income)
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
