package com.mo.cashback.ui.categories

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.mo.cashback.data.EntryWithRefs
import com.mo.cashback.ui.banks.formatPercent
import com.mo.cashback.ui.component.AppTopBar
import com.mo.cashback.ui.component.appViewModel
import com.mo.cashback.ui.component.displayName
import com.mo.cashback.ui.component.monthLabel
import com.mo.cashback.ui.component.parseHex
import com.mo.cashback.ui.dialog.AddOrEditEntryDialog

@Composable
fun CategoryDetailScreen(categoryId: String, onBack: () -> Unit) {
    val vm = appViewModel(key = "cat_$categoryId") { CategoryDetailViewModel(it, categoryId) }
    val cat by vm.category.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    val banks by vm.banks.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    var showConfirm by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = cat?.let { "${it.emoji} ${it.displayName()}" } ?: "",
                onBack = onBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
            }
        },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.category_best_in_month, monthLabel(vm.year, vm.month).lowercase()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            itemsIndexed(entries) { idx, row -> RankRow(idx + 1, row, onDelete = { vm.deleteEntry(row.entry.id) }) }
            if (entries.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.category_other_banks_none),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
            val currentCat = cat
            if (currentCat != null && !currentCat.isBuiltIn) {
                item {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showConfirm = true }
                            .padding(horizontal = 4.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.delete_category_action, currentCat.displayName()),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Text(
                        text = stringResource(R.string.delete_category_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        }

        if (showAddDialog) {
            AddOrEditEntryDialog(
                lockedBankId = null,
                lockedCategoryId = categoryId,
                banks = banks,
                categories = categories,
                onDismiss = { showAddDialog = false; errorMsg = null },
                onAddCategory = { _, _ -> /* hidden when category is locked */ },
                onConfirm = { bankId, _, pct ->
                    vm.addEntry(
                        bankId, pct,
                        onError = { errorMsg = it.message ?: it.javaClass.simpleName },
                        onOk = { showAddDialog = false; errorMsg = null },
                    )
                },
                errorMessage = errorMsg,
            )
        }

        if (showConfirm) {
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                title = { Text(stringResource(R.string.delete_category_action, cat?.displayName() ?: "")) },
                text = { Text(stringResource(R.string.delete_category_hint)) },
                confirmButton = {
                    TextButton(onClick = { vm.delete(onDone = onBack) }) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirm = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun RankRow(rank: Int, row: EntryWithRefs, onDelete: () -> Unit) {
    val color = parseHex(row.bank.colorHex)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFFECECEF)),
            contentAlignment = Alignment.Center,
        ) {
            Text("$rank", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color)
                .border(1.dp, Color(0x22000000), RoundedCornerShape(10.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Text(row.bank.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(formatPercent(row.entry.percent), color = MaterialTheme.colorScheme.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.delete))
        }
    }
}
