package com.mo.cashback.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mo.cashback.R
import com.mo.cashback.data.Bank
import com.mo.cashback.data.Category
import com.mo.cashback.ui.component.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditEntryDialog(
    lockedBankId: String?,
    lockedCategoryId: String? = null,
    banks: List<Bank>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onAddCategory: (name: String, emoji: String) -> Unit,
    onConfirm: (bankId: String, categoryId: String, percent: Double) -> Unit,
    errorMessage: String? = null,
) {
    var bankId by remember { mutableStateOf(lockedBankId ?: banks.firstOrNull()?.id ?: "") }
    var bankMenu by remember { mutableStateOf(false) }
    var categoryId by remember { mutableStateOf<String?>(lockedCategoryId) }
    var categoryMenu by remember { mutableStateOf(false) }
    var percentText by remember { mutableStateOf("") }
    var showNewCatDialog by remember { mutableStateOf(false) }

    val selectedBank = banks.firstOrNull { it.id == bankId }
    val selectedCategory = categories.firstOrNull { it.id == categoryId }
    val parsedPercent = percentText.replace(',', '.').toDoubleOrNull()
    val canConfirm = selectedBank != null && selectedCategory != null && parsedPercent != null && parsedPercent > 0

    val bankLocked = lockedBankId != null
    val categoryLocked = lockedCategoryId != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_cashback_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // bank field
                ExposedDropdownMenuBox(
                    expanded = bankMenu,
                    onExpandedChange = { if (!bankLocked) bankMenu = it },
                ) {
                    OutlinedTextField(
                        value = selectedBank?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = !bankLocked,
                        label = { Text(stringResource(R.string.field_bank)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bankMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = !bankLocked),
                    )
                    ExposedDropdownMenu(
                        expanded = bankMenu,
                        onDismissRequest = { bankMenu = false },
                    ) {
                        banks.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b.name) },
                                onClick = { bankId = b.id; bankMenu = false },
                            )
                        }
                    }
                }

                // category field
                ExposedDropdownMenuBox(
                    expanded = categoryMenu,
                    onExpandedChange = { if (!categoryLocked) categoryMenu = it },
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.let { "${it.emoji} ${it.displayName()}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = !categoryLocked,
                        label = { Text(stringResource(R.string.field_category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = !categoryLocked),
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenu,
                        onDismissRequest = { categoryMenu = false },
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.emoji} ${c.displayName()}") },
                                onClick = { categoryId = c.id; categoryMenu = false },
                            )
                        }
                        if (!categoryLocked) {
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.new_category_option), color = MaterialTheme.colorScheme.primary) },
                                onClick = { categoryMenu = false; showNewCatDialog = true },
                            )
                        }
                    }
                }

                // percent field
                OutlinedTextField(
                    value = percentText,
                    onValueChange = { percentText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                    label = { Text(stringResource(R.string.field_percent)) },
                    supportingText = { Text(stringResource(R.string.percent_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = percentText.isNotBlank() && parsedPercent == null,
                )

                if (errorMessage != null) {
                    Text(
                        text = stringResource(R.string.entry_duplicate_error),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (canConfirm) onConfirm(bankId, categoryId!!, parsedPercent!!) },
                enabled = canConfirm,
            ) { Text(stringResource(R.string.add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )

    if (showNewCatDialog) {
        NewCategoryDialog(
            onDismiss = { showNewCatDialog = false },
            onCreate = { name, emoji ->
                onAddCategory(name, emoji)
                showNewCatDialog = false
            },
        )
    }
}
