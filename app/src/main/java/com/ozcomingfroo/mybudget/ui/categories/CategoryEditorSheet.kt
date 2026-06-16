package com.ozcomingfroo.mybudget.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Undo
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.model.CategoryType

internal data class CategoryEditorForm(
    val title: String,
    val type: CategoryType,
    val iconName: String,
    val color: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryEditorSheet(
    category: CategoryEntity?,
    initialType: CategoryType,
    onSave: (CategoryEditorForm) -> Unit,
    onDelete: (CategoryEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by rememberSaveable(category?.id, initialType) {
        mutableStateOf(category?.title.orEmpty())
    }
    var type by rememberSaveable(category?.id, initialType) {
        mutableStateOf(
            when (category?.type) {
                CategoryType.INCOME -> CategoryType.INCOME
                else -> initialType
            },
        )
    }
    var selectedIconName by rememberSaveable(category?.id, initialType) {
        mutableStateOf(category?.iconName ?: CategoryEditorIconOptions.first().iconName)
    }
    var selectedColor by rememberSaveable(category?.id, initialType) {
        mutableStateOf(category?.color ?: CategoryColorOptions.first())
    }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val canSave = title.trim().isNotBlank()
    val strings = CategoryEditorStrings(
        sheetTitle = stringResource(
            if (category == null) R.string.create_category else R.string.edit_category,
        ),
        categoryName = stringResource(R.string.category_name),
        categoryType = stringResource(R.string.category_type),
        categoryIcon = stringResource(R.string.category_icon),
        categoryColor = stringResource(R.string.category_color),
        expense = stringResource(R.string.expense),
        income = stringResource(R.string.income),
        delete = stringResource(R.string.delete),
        cancel = stringResource(R.string.cancel),
        save = stringResource(R.string.save),
        deleteCategoryTitle = stringResource(R.string.delete_category_title),
        deleteCategoryMessage = category?.let {
            stringResource(R.string.delete_category_message, it.title)
        }.orEmpty(),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
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
                    text = strings.sheetTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.categoryName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                CategoryEditorSectionTitle(text = strings.categoryType)
                CategoryTypeSwitch(
                    selectedType = type,
                    onTypeSelected = { type = it },
                    modifier = Modifier.testTag("category_editor_type_selector"),
                    labels = CategoryTypeSwitchLabels(
                        expense = strings.expense,
                        income = strings.income,
                    ),
                )
                CategoryEditorSectionTitle(text = strings.categoryIcon)
                CategoryIconPicker(
                    selectedIconName = selectedIconName,
                    onIconSelected = { selectedIconName = it },
                )
                CategoryEditorSectionTitle(text = strings.categoryColor)
                CategoryColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it },
                )
            }

            CategoryEditorActions(
                category = category,
                canSave = canSave,
                strings = strings,
                onDelete = { showDeleteConfirmation = true },
                onCancel = onDismiss,
                onSave = {
                    onSave(
                        CategoryEditorForm(
                            title = title,
                            type = type,
                            iconName = selectedIconName,
                            color = selectedColor,
                        ),
                    )
                },
            )
        }
    }

    if (showDeleteConfirmation && category != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(strings.deleteCategoryTitle) },
            text = { Text(strings.deleteCategoryMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete(category)
                    },
                ) {
                    Text(strings.delete)
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
private fun CategoryEditorSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun CategoryEditorActions(
    category: CategoryEntity?,
    canSave: Boolean,
    strings: CategoryEditorStrings,
    onDelete: () -> Unit,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (category != null) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("category_editor_delete"),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                ) {
                    Text(
                        text = strings.delete,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .testTag("category_editor_cancel"),
            ) {
                Text(strings.cancel)
            }
            Button(
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier
                    .weight(1f)
                    .testTag("category_editor_save"),
            ) {
                Text(strings.save)
            }
        }
    }
}

private data class CategoryEditorStrings(
    val sheetTitle: String,
    val categoryName: String,
    val categoryType: String,
    val categoryIcon: String,
    val categoryColor: String,
    val expense: String,
    val income: String,
    val delete: String,
    val cancel: String,
    val save: String,
    val deleteCategoryTitle: String,
    val deleteCategoryMessage: String,
)

@Composable
private fun CategoryIconPicker(
    selectedIconName: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CategoryEditorIconOptions.chunked(5).forEach { rowIcons ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowIcons.forEach { option ->
                    CategoryIconOptionButton(
                        option = option,
                        selected = selectedIconName == option.iconName,
                        onClick = { onIconSelected(option.iconName) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(5 - rowIcons.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryIconOptionButton(
    option: CategoryEditorIconOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = option.imageVector,
                contentDescription = option.contentDescription,
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun CategoryColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CategoryColorOptions.chunked(6).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowColors.forEach { colorValue ->
                    val color = colorValue.toColor()
                    val selected = selectedColor == colorValue
                    Card(
                        onClick = { onColorSelected(colorValue) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        border = BorderStroke(
                            if (selected) 3.dp else 1.dp,
                            if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        ),
                        colors = CardDefaults.cardColors(containerColor = color),
                    ) {}
                }
                repeat(6 - rowColors.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class CategoryEditorIconOption(
    val iconName: String,
    val imageVector: ImageVector,
    val contentDescription: String,
)

private val CategoryEditorIconOptions = listOf(
    CategoryEditorIconOption("shopping_cart", Icons.Filled.ShoppingCart, "Shopping cart"),
    CategoryEditorIconOption("restaurant", Icons.Filled.Restaurant, "Restaurant"),
    CategoryEditorIconOption("home", Icons.Filled.Home, "Home"),
    CategoryEditorIconOption("receipt", Icons.Filled.Receipt, "Receipt"),
    CategoryEditorIconOption("phone_android", Icons.Filled.PhoneAndroid, "Phone"),
    CategoryEditorIconOption("directions_car", Icons.Filled.DirectionsCar, "Car"),
    CategoryEditorIconOption("medical_services", Icons.Filled.LocalHospital, "Health"),
    CategoryEditorIconOption("shopping_bag", Icons.Filled.ShoppingBag, "Shopping bag"),
    CategoryEditorIconOption("movie", Icons.Filled.Movie, "Movie"),
    CategoryEditorIconOption("payments", Icons.Filled.Payments, "Payments"),
    CategoryEditorIconOption("work", Icons.Filled.Work, "Work"),
    CategoryEditorIconOption("attach_money", Icons.Filled.AttachMoney, "Money"),
    CategoryEditorIconOption("undo", Icons.AutoMirrored.Filled.Undo, "Refund"),
    CategoryEditorIconOption("card_giftcard", Icons.Filled.CardGiftcard, "Gift"),
    CategoryEditorIconOption("sports", Icons.Filled.SportsSoccer, "Sports"),
    CategoryEditorIconOption("education", Icons.Filled.School, "Education"),
    CategoryEditorIconOption("tech", Icons.Filled.SportsEsports, "Tech"),
    CategoryEditorIconOption("category", Icons.Filled.Category, "Category"),
    CategoryEditorIconOption("other", Icons.AutoMirrored.Filled.Help, "Other"),
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

private fun String.toColor(): Color =
    runCatching { Color(android.graphics.Color.parseColor(this)) }
        .getOrDefault(Color.Gray)
