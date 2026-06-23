package com.ozcomingfroo.mybudget.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.preferences.AppLanguageMode
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import com.ozcomingfroo.mybudget.data.repository.BudgetBookRepository
import com.ozcomingfroo.mybudget.data.repository.StarterCategoryTitle
import com.ozcomingfroo.mybudget.ui.onboarding.BudgetBookDetailsFields
import com.ozcomingfroo.mybudget.ui.onboarding.StarterCategoryResources
import java.util.Locale
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
private val HebrewLocale = Locale("iw")

@Composable
internal fun AccountsScreen(
    budgetBooks: List<BudgetBookEntity>,
    archivedBudgetBooks: List<BudgetBookEntity>,
    selectedBudgetBookId: Long?,
    languageMode: AppLanguageMode,
    appPreferencesRepository: AppPreferencesRepository,
    budgetBookRepository: BudgetBookRepository,
    snackbarHostState: SnackbarHostState,
    onCreateAccount: () -> Unit,
) {
    val context = LocalContext.current
    val accountResourceContext = remember(context, languageMode) { context.localizedFor(languageMode) }
    val scope = rememberCoroutineScope()
    var editingBudgetBook by remember { mutableStateOf<BudgetBookEntity?>(null) }
    var restoringBudgetBook by remember { mutableStateOf<BudgetBookEntity?>(null) }
    var deletingArchivedBudgetBook by remember { mutableStateOf<BudgetBookEntity?>(null) }
    var selectedAccountView by rememberSaveable { mutableStateOf(AccountListView.Active) }
    val accountSwitchedMessage = accountResourceContext.getString(R.string.account_switched)
    val accountUpdatedMessage = accountResourceContext.getString(R.string.account_updated)
    val accountArchivedMessage = accountResourceContext.getString(R.string.account_archived)
    val accountDeletedMessage = accountResourceContext.getString(R.string.account_deleted_permanently)
    val accountRestoredMessage = accountResourceContext.getString(R.string.account_restored)
    val blockedMessage = accountResourceContext.getString(R.string.account_delete_blocked)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onCreateAccount,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(stringResource(R.string.create_account))
        }
        AccountViewSelector(
            selectedView = selectedAccountView,
            onViewSelected = { selectedAccountView = it },
        )
        when (selectedAccountView) {
            AccountListView.Active -> {
                if (budgetBooks.isEmpty()) {
                    AccountEmptyState(text = stringResource(R.string.no_accounts_yet))
                } else {
                    budgetBooks.forEach { budgetBook ->
                        ActiveAccountRow(
                            budgetBook = budgetBook,
                            isSelected = budgetBook.id == selectedBudgetBookId,
                            onSwitch = {
                                scope.launch {
                                    appPreferencesRepository.setSelectedBudgetBookId(budgetBook.id)
                                    snackbarHostState.showSnackbar(accountSwitchedMessage)
                                }
                            },
                            onEdit = { editingBudgetBook = budgetBook },
                        )
                    }
                }
            }

            AccountListView.Archived -> {
                if (archivedBudgetBooks.isEmpty()) {
                    AccountEmptyState(text = stringResource(R.string.no_archived_accounts_yet))
                } else {
                    archivedBudgetBooks.forEach { budgetBook ->
                        ArchivedAccountRow(
                            budgetBook = budgetBook,
                            onRestore = { restoringBudgetBook = budgetBook },
                            onDeletePermanently = { deletingArchivedBudgetBook = budgetBook },
                        )
                    }
                }
            }
        }
    }

    editingBudgetBook?.let { budgetBook ->
        AccountEditorSheet(
            budgetBook = budgetBook,
            canRemove = budgetBook.id != selectedBudgetBookId && budgetBooks.size > 1,
            strings = accountEditorStrings(
                context = accountResourceContext,
                accountTitle = budgetBook.title,
            ),
            onSave = { title, description ->
                scope.launch {
                    budgetBookRepository.updateBudgetBookDetails(
                        id = budgetBook.id,
                        title = title,
                        description = description,
                    )
                    editingBudgetBook = null
                    snackbarHostState.showSnackbar(accountUpdatedMessage)
                }
            },
            onArchive = {
                scope.launch {
                    val archived = budgetBookRepository.archiveBudgetBook(budgetBook.id)
                    editingBudgetBook = null
                    snackbarHostState.showSnackbar(if (archived) accountArchivedMessage else blockedMessage)
                }
            },
            onDeletePermanently = {
                scope.launch {
                    val deleted = budgetBookRepository.deleteBudgetBookPermanently(budgetBook.id)
                    editingBudgetBook = null
                    snackbarHostState.showSnackbar(if (deleted) accountDeletedMessage else blockedMessage)
                }
            },
            onDismiss = { editingBudgetBook = null },
        )
    }

    restoringBudgetBook?.let { budgetBook ->
        AlertDialog(
            onDismissRequest = { restoringBudgetBook = null },
            title = { Text(accountResourceContext.getString(R.string.restore_account_title)) },
            text = { Text(accountResourceContext.getString(R.string.restore_account_message, budgetBook.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val restored = budgetBookRepository.restoreBudgetBook(budgetBook.id)
                            restoringBudgetBook = null
                            if (restored) {
                                selectedAccountView = AccountListView.Active
                                snackbarHostState.showSnackbar(accountRestoredMessage)
                            }
                        }
                    },
                ) {
                    Text(accountResourceContext.getString(R.string.restore_account))
                }
            },
            dismissButton = {
                TextButton(onClick = { restoringBudgetBook = null }) {
                    Text(accountResourceContext.getString(R.string.cancel))
                }
            },
        )
    }

    deletingArchivedBudgetBook?.let { budgetBook ->
        AlertDialog(
            onDismissRequest = { deletingArchivedBudgetBook = null },
            title = { Text(accountResourceContext.getString(R.string.delete_account_permanently_title)) },
            text = {
                Text(accountResourceContext.getString(R.string.delete_account_permanently_message, budgetBook.title))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val deleted = budgetBookRepository.deleteBudgetBookPermanently(budgetBook.id)
                            deletingArchivedBudgetBook = null
                            if (deleted) {
                                snackbarHostState.showSnackbar(accountDeletedMessage)
                            }
                        }
                    },
                ) {
                    Text(accountResourceContext.getString(R.string.delete_account_permanently))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingArchivedBudgetBook = null }) {
                    Text(accountResourceContext.getString(R.string.cancel))
                }
            },
        )
    }
}

@Composable
internal fun CreateAccountScreen(
    initialSeedLanguageMode: AppLanguageMode,
    createBudgetBook: suspend (String, String?, List<StarterCategoryTitle>) -> Long,
    snackbarHostState: SnackbarHostState,
    onCreated: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val seedResourceContext = context.applicationContext ?: context
    val scope = rememberCoroutineScope()
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var seedLanguageMode by rememberSaveable { mutableStateOf(initialSeedLanguageMode) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val canSave = title.trim().isNotBlank() && !isSaving
    val createdMessage = stringResource(R.string.account_created)
    val cancelLabel = stringResource(R.string.create_account_cancel)
    val submitLabel = stringResource(R.string.create_account_submit)

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.create_account_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.create_account_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BudgetBookDetailsFields(
                title = title,
                onTitleChange = { title = it },
                description = description,
                onDescriptionChange = { description = it },
                titleSupportingText = stringResource(R.string.budget_book_name_helper),
                titleTestTag = "create_account_title",
            )
            SeedLanguageSelector(
                selectedLanguageMode = seedLanguageMode,
                onLanguageModeSelected = { seedLanguageMode = it },
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !isSaving,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp)
                    .testTag("create_account_cancel"),
            ) {
                Text(
                    text = cancelLabel,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Button(
                onClick = {
                    if (isSaving || title.trim().isBlank()) return@Button
                    isSaving = true
                    scope.launch {
                        try {
                            createBudgetBook(
                                title,
                                description,
                                StarterCategoryResources.resolveTitles(
                                    context = seedResourceContext,
                                    languageMode = seedLanguageMode,
                                ),
                            )
                            onCreated()
                            snackbarHostState.showSnackbar(createdMessage)
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp)
                    .testTag("create_account_save"),
            ) {
                Text(
                    text = submitLabel,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SeedLanguageSelector(
    selectedLanguageMode: AppLanguageMode,
    onLanguageModeSelected: (AppLanguageMode) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.starter_categories_language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.starter_categories_language_helper),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppLanguageMode.entries.forEach { languageMode ->
                SeedLanguageOption(
                    languageMode = languageMode,
                    selected = selectedLanguageMode == languageMode,
                    onClick = { onLanguageModeSelected(languageMode) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SeedLanguageOption(
    languageMode: AppLanguageMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = languageMode.label()
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = modifier
            .heightIn(min = 56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .testTag("create_account_seed_language_${languageMode.name}"),
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (languageMode) {
                AppLanguageMode.SYSTEM -> Text(
                    text = stringResource(R.string.language_system_short),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )

                AppLanguageMode.EN_US -> Image(
                    painter = painterResource(R.drawable.flag_us),
                    contentDescription = label,
                    modifier = Modifier.size(width = 42.dp, height = 28.dp),
                )

                AppLanguageMode.HE -> Image(
                    painter = painterResource(R.drawable.flag_israel),
                    contentDescription = label,
                    modifier = Modifier.size(width = 42.dp, height = 28.dp),
                )
            }
        }
    }
}

private enum class AccountListView {
    Active,
    Archived,
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AccountViewSelector(
    selectedView: AccountListView,
    onViewSelected: (AccountListView) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        AccountListView.entries.forEachIndexed { index, view ->
            SegmentedButton(
                selected = selectedView == view,
                onClick = { onViewSelected(view) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = AccountListView.entries.size,
                ),
            ) {
                Text(
                    stringResource(
                        when (view) {
                            AccountListView.Active -> R.string.active_accounts
                            AccountListView.Archived -> R.string.archived_accounts
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun AccountEmptyState(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ActiveAccountRow(
    budgetBook: BudgetBookEntity,
    isSelected: Boolean,
    onSwitch: () -> Unit,
    onEdit: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budgetBook.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    budgetBook.description?.takeIf { it.isNotBlank() }?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (isSelected) {
                    Text(
                        text = stringResource(R.string.current_account),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FilledTonalButton(
                    onClick = onSwitch,
                    enabled = !isSelected,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwitchAccount,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(stringResource(R.string.switch_account))
                }
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(stringResource(R.string.edit_account))
                }
            }
        }
    }
}

@Composable
private fun ArchivedAccountRow(
    budgetBook: BudgetBookEntity,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budgetBook.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    budgetBook.description?.takeIf { it.isNotBlank() }?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.archived_account),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = onRestore,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Restore,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(stringResource(R.string.restore_account))
                }
                OutlinedButton(
                    onClick = onDeletePermanently,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = stringResource(R.string.delete_account_permanently),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun accountEditorStrings(
    context: android.content.Context,
    accountTitle: String,
): AccountEditorStrings {
    return AccountEditorStrings(
        editAccount = context.getString(R.string.edit_account),
        budgetBookName = context.getString(R.string.budget_book_name),
        budgetBookNameHelper = context.getString(R.string.budget_book_name_helper),
        budgetBookDescription = context.getString(R.string.budget_book_description),
        budgetBookDescriptionHelper = context.getString(R.string.budget_book_description_helper),
        accountDeleteBlocked = context.getString(R.string.account_delete_blocked),
        archive = context.getString(R.string.archive),
        archiveAccountTitle = context.getString(R.string.archive_account_title),
        archiveAccountMessage = context.getString(R.string.archive_account_message, accountTitle),
        deleteAccountPermanently = context.getString(R.string.delete_account_permanently),
        deleteAccountPermanentlyTitle = context.getString(R.string.delete_account_permanently_title),
        deleteAccountPermanentlyMessage = context.getString(
            R.string.delete_account_permanently_message,
            accountTitle,
        ),
        cancel = context.getString(R.string.cancel),
        save = context.getString(R.string.save),
    )
}

private fun android.content.Context.localizedFor(languageMode: AppLanguageMode): android.content.Context {
    val locale = when (languageMode) {
        AppLanguageMode.SYSTEM -> return this
        AppLanguageMode.EN_US -> Locale.US
        AppLanguageMode.HE -> HebrewLocale
    }
    val configuration = android.content.res.Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}

internal data class AccountEditorStrings(
    val editAccount: String,
    val budgetBookName: String,
    val budgetBookNameHelper: String,
    val budgetBookDescription: String,
    val budgetBookDescriptionHelper: String,
    val accountDeleteBlocked: String,
    val archive: String,
    val archiveAccountTitle: String,
    val archiveAccountMessage: String,
    val deleteAccountPermanently: String,
    val deleteAccountPermanentlyTitle: String,
    val deleteAccountPermanentlyMessage: String,
    val cancel: String,
    val save: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountEditorSheet(
    budgetBook: BudgetBookEntity,
    canRemove: Boolean,
    strings: AccountEditorStrings,
    onSave: (String, String?) -> Unit,
    onArchive: () -> Unit,
    onDeletePermanently: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by rememberSaveable(budgetBook.id) { mutableStateOf(budgetBook.title) }
    var description by rememberSaveable(budgetBook.id) { mutableStateOf(budgetBook.description.orEmpty()) }
    var showArchiveConfirmation by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val canSave = title.trim().isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = strings.editAccount,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                BudgetBookDetailsFields(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    titleSupportingText = strings.budgetBookNameHelper,
                    titleLabel = strings.budgetBookName,
                    descriptionLabel = strings.budgetBookDescription,
                    descriptionSupportingText = strings.budgetBookDescriptionHelper,
                )
                if (!canRemove) {
                    Text(
                        text = strings.accountDeleteBlocked,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            AccountEditorActions(
                canSave = canSave,
                canRemove = canRemove,
                strings = strings,
                onArchive = { showArchiveConfirmation = true },
                onDeletePermanently = { showDeleteConfirmation = true },
                onCancel = onDismiss,
                onSave = { onSave(title, description) },
            )
        }
    }

    if (showArchiveConfirmation) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirmation = false },
            title = { Text(strings.archiveAccountTitle) },
            text = { Text(strings.archiveAccountMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showArchiveConfirmation = false
                        onArchive()
                    },
                ) {
                    Text(strings.archive)
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveConfirmation = false }) {
                    Text(strings.cancel)
                }
            },
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(strings.deleteAccountPermanentlyTitle) },
            text = { Text(strings.deleteAccountPermanentlyMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeletePermanently()
                    },
                ) {
                    Text(strings.deleteAccountPermanently)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(strings.cancel)
                }
            },
        )
    }
}

@Composable
private fun AccountEditorActions(
    canSave: Boolean,
    canRemove: Boolean,
    strings: AccountEditorStrings,
    onArchive: () -> Unit,
    onDeletePermanently: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .imePadding(),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onArchive,
                enabled = canRemove,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(strings.archive)
            }
            OutlinedButton(
                onClick = onDeletePermanently,
                enabled = canRemove,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(
                    text = strings.deleteAccountPermanently,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            ) {
                Text(strings.cancel)
            }
            Button(
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier.weight(1f),
            ) {
                Text(strings.save)
            }
        }
    }
}
