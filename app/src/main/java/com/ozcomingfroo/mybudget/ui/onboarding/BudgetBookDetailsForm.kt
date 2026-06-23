package com.ozcomingfroo.mybudget.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.ozcomingfroo.mybudget.R

@Composable
internal fun BudgetBookDetailsFields(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    titleSupportingText: String,
    modifier: Modifier = Modifier,
    titleLabel: String? = null,
    descriptionLabel: String? = null,
    descriptionSupportingText: String? = null,
    titleTestTag: String? = null,
    descriptionTestTag: String? = null,
) {
    val resolvedTitleLabel = titleLabel ?: stringResource(R.string.budget_book_name)
    val resolvedDescriptionLabel = descriptionLabel ?: stringResource(R.string.budget_book_description)
    val resolvedDescriptionSupportingText =
        descriptionSupportingText ?: stringResource(R.string.budget_book_description_helper)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(resolvedTitleLabel) },
            supportingText = { Text(titleSupportingText) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(titleTestTag?.let { Modifier.testTag(it) } ?: Modifier),
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(resolvedDescriptionLabel) },
            supportingText = { Text(resolvedDescriptionSupportingText) },
            minLines = 2,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(descriptionTestTag?.let { Modifier.testTag(it) } ?: Modifier),
        )
    }
}
