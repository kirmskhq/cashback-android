package com.mo.cashback.ui.banks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mo.cashback.R
import com.mo.cashback.data.Bank
import com.mo.cashback.ui.component.AppTopBar
import com.mo.cashback.ui.component.appViewModel
import com.mo.cashback.ui.component.parseHex
import com.mo.cashback.ui.dialog.AddCustomBankDialog

@Composable
fun BankPickerScreen(onBack: () -> Unit) {
    val vm = appViewModel { BankPickerViewModel(it) }
    val all by vm.allBanks.collectAsStateWithLifecycle()
    val customCount by vm.customCount.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var showCustomDialog by remember { mutableStateOf(false) }

    val filtered = remember(all, query) {
        if (query.isBlank()) all
        else all.filter { it.name.contains(query, ignoreCase = true) }
    }
    val catalog = filtered.filter { it.isBuiltIn }
    val custom = filtered.filter { !it.isBuiltIn }
    val customLimitReached = customCount >= 3

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.nav_banks_picker),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                },
            )
        },
    ) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.picker_search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (catalog.isNotEmpty()) {
                    items(catalog, key = { it.id }) { bank ->
                        BankRow(bank, onToggle = { vm.toggleSelected(bank) }, onDelete = null)
                    }
                }
                if (custom.isNotEmpty()) {
                    item {
                        SectionHeader(stringResource(R.string.picker_custom_section))
                    }
                    items(custom, key = { it.id }) { bank ->
                        BankRow(bank, onToggle = { vm.toggleSelected(bank) }, onDelete = { vm.deleteCustom(bank) })
                    }
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    AddCustomRow(
                        enabled = !customLimitReached,
                        currentCount = customCount,
                        onClick = { showCustomDialog = true },
                    )
                }
            }
        }

        if (showCustomDialog) {
            AddCustomBankDialog(
                onDismiss = { showCustomDialog = false },
                onCreate = { name, colorHex ->
                    vm.addCustom(name, colorHex) { ok ->
                        if (ok) showCustomDialog = false
                    }
                },
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 14.dp, bottom = 6.dp),
    )
}

@Composable
private fun BankRow(bank: Bank, onToggle: () -> Unit, onDelete: (() -> Unit)?) {
    val color = parseHex(bank.colorHex)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = bank.isSelected, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .border(1.dp, Color(0x22000000), RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = bank.name,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
        )
        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AddCustomRow(enabled: Boolean, currentCount: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.picker_add_custom),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!enabled) {
            Text(
                text = stringResource(R.string.picker_custom_limit, currentCount),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 40.dp, top = 4.dp),
            )
        }
    }
}
