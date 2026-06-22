package com.ozcomingfroo.mybudget.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.preferences.AppPreferences
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import com.ozcomingfroo.mybudget.data.repository.BudgetBookRepository
import com.ozcomingfroo.mybudget.ui.onboarding.BudgetBookDetailsFields
import com.ozcomingfroo.mybudget.ui.onboarding.StarterCategoryResources
import kotlinx.coroutines.launch

@Composable
internal fun AccountsScreen(
    budgetBooks: List<BudgetBookEntity>,
    archivedBudgetBooks: List<BudgetBookEntity>,
    selectedBudgetBookId: Long?,
    appPreferencesRepository: AppPreferencesRepository,
    budgetBookRepository: BudgetBookRepository,
    snackbarHostState: SnackbarHostState,
    onCreateAccount: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var editingBudgetBook by remember { mutableStateOf<BudgetBookEntity?>(null) }
    var restoringBudgetBook by remember { mutableStateOf<BudgetBookEntity?>(null) }
    var deletingArchivedBudgetBook by remember { mutableStateOf<BudgetBookEntity?>(null) }
    var showCreateConfirmation by remember { mutableStateOf(false) }
    var selectedAccountView by rememberSaveable { mutableStateOf(AccountListView.Active) }
    val accountSwitchedMessage = stringResource(R.string.account_switched)
    val accountUpdatedMessage = stringResource(R.string.account_updated)
    val accountArchivedMessage = stringResource(R.string.account_archived)
    val accountDeletedMessage = stringResource(R.string.account_deleted_permanently)
    val accountRestoredMessage = stringResource(R.string.account_restored)
    val blockedMessage = stringResource(R.string.account_delete_blocked)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = { showCreateConfirmation = true },
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
            title = { Text(stringResource(R.string.restore_account_title)) },
            text = { Text(stringResource(R.string.restore_account_message, budgetBook.title)) },
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
                    Text(stringResource(R.string.restore_account))
                }
            },
            dismissButton = {
                TextButton(onClick = { restoringBudgetBook = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    deletingArchivedBudgetBook?.let { budgetBook ->
        AlertDialog(
            onDismissRequest = { deletingArchivedBudgetBook = null },
            title = { Text(stringResource(R.string.delete_account_permanently_title)) },
            text = { Text(stringResource(R.string.delete_account_permanently_message, budgetBook.title)) },
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
                    Text(stringResource(R.string.delete_account_permanently))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingArchivedBudgetBook = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showCreateConfirmation) {
        AlertDialog(
            onDismissRequest = { showCreateConfirmation = false },
            title = { Text(stringResource(R.string.create_account_confirmation_title)) },
            text = { Text(stringResource(R.string.create_account_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCreateConfirmation = false
                        onCreateAccount()
                    },
                ) {
                    Text(stringResource(R.string.create_account))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
internal fun CreateAccountScreen(
    preferences: AppPreferences,
    budgetBookRepository: BudgetBookRepository,
    snackbarHostState: SnackbarHostState,
    onCreated: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    val canSave = title.trim().isNotBlank()
    val createdMessage = stringResource(R.string.account_created)

    Column(
        modifier = Modifier
            .fillMaxSize()
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
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.cancel))
            }
            Button(
                onClick = {
                    scope.launch {
                        budgetBookRepository.createBudgetBook(
                            title = title,
                            description = description,
                            selectAfterCreate = true,
                            starterCategoryTitles = StarterCategoryResources.resolveTitles(
                                context = context,
                                languageMode = preferences.languageMode,
                            ),
                        )
                        snackbarHostState.showSnackbar(createdMessage)
                        onCreated()
                    }
                },
                enabled = canSave,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.save))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountEditorSheet(
    budgetBook: BudgetBookEntity,
    canRemove: Boolean,
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
                    text = stringResource(R.string.edit_account),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                BudgetBookDetailsFields(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    titleSupportingText = stringResource(R.string.budget_book_name_helper),
                )
                if (!canRemove) {
                    Text(
                        text = stringResource(R.string.account_delete_blocked),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            AccountEditorActions(
                canSave = canSave,
                canRemove = canRemove,
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
            title = { Text(stringResource(R.string.archive_account_title)) },
            text = { Text(stringResource(R.string.archive_account_message, budgetBook.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showArchiveConfirmation = false
                        onArchive()
                    },
                ) {
                    Text(stringResource(R.string.archive))
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_account_permanently_title)) },
            text = { Text(stringResource(R.string.delete_account_permanently_message, budgetBook.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeletePermanently()
                    },
                ) {
                    Text(stringResource(R.string.delete_account_permanently))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun AccountEditorActions(
    canSave: Boolean,
    canRemove: Boolean,
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
                Text(stringResource(R.string.archive))
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
                    text = stringResource(R.string.delete_account_permanently),
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
                Text(stringResource(R.string.cancel))
            }
            Button(
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
