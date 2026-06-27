package com.mo.cashback.ui.banks

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mo.cashback.R
import com.mo.cashback.data.EntryWithRefs
import com.mo.cashback.ui.component.AppTopBar
import com.mo.cashback.ui.component.appViewModel
import com.mo.cashback.ui.component.bankColor
import com.mo.cashback.ui.component.bestForegroundOn
import com.mo.cashback.ui.component.displayName
import com.mo.cashback.ui.component.monthLabel
import com.mo.cashback.ui.dialog.AddOrEditEntryDialog

@Composable
fun BankDetailScreen(bankId: String, onBack: () -> Unit) {
    val vm = appViewModel(key = "bank_$bankId") { BankDetailViewModel(it, bankId) }
    val bank by vm.bank.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    val prevEntries by vm.previousMonthEntries.collectAsStateWithLifecycle()
    val cats by vm.categories.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var carryForwardDismissed by remember { mutableStateOf(false) }
    val showCarryForward = entries.isEmpty() && prevEntries.isNotEmpty() && !carryForwardDismissed

    val color = bank?.let { bankColor(it) } ?: MaterialTheme.colorScheme.primary
    val on = color.bestForegroundOn()

    Scaffold(
        topBar = {
            AppTopBar(
                title = bank?.name ?: "",
                onBack = onBack,
                containerColor = color,
                contentColor = on,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
            }
        },
    ) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            // header band
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color)
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp),
            ) {
                Text(
                    text = monthLabel(vm.year, vm.month).uppercase(),
                    color = on.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (entries.isEmpty())
                        stringResource(R.string.bank_no_entries_title)
                    else
                        "${entries.size} ${countWord(entries.size)}",
                    color = on,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (entries.isEmpty())
                        stringResource(R.string.bank_no_entries_hint)
                    else
                        stringResource(R.string.bank_detail_subtitle),
                    color = on.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                )
            }

            if (showCarryForward) {
                CarryForwardBanner(
                    prevMonthLabel = monthLabel(vm.prevYear, vm.prevMonth),
                    entries = prevEntries,
                    onCopy = { vm.copyFromPreviousMonth() },
                    onDismiss = { carryForwardDismissed = true },
                )
            }
            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.empty_month), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(entries, key = { it.entry.id }) { row ->
                        EntryRow(row, onDelete = { vm.deleteEntry(row.entry.id) })
                        HorizontalDivider(color = Color(0xFFECECEF))
                    }
                }
            }
        }

        if (showDialog) {
            AddOrEditEntryDialog(
                lockedBankId = bankId,
                banks = listOfNotNull(bank),
                categories = cats,
                onDismiss = { showDialog = false; errorMsg = null },
                onAddCategory = { name, emoji -> vm.addCustomCategory(name, emoji) },
                onConfirm = { _, catId, pct ->
                    vm.addEntry(
                        catId, pct,
                        onError = { errorMsg = it.message ?: it.javaClass.simpleName },
                        onOk = { showDialog = false; errorMsg = null },
                    )
                },
                errorMessage = errorMsg,
            )
        }
    }
}

@Composable
private fun EntryRow(row: EntryWithRefs, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(row.category.emoji, fontSize = 22.sp)
        Spacer(Modifier.width(14.dp))
        Text(row.category.displayName(), modifier = Modifier.weight(1f), fontSize = 15.sp)
        Text(
            text = formatPercent(row.entry.percent),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.delete))
        }
    }
}

@Composable
private fun CarryForwardBanner(
    prevMonthLabel: String,
    entries: List<EntryWithRefs>,
    onCopy: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.carry_forward_title, prevMonthLabel),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.cancel),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = entries.joinToString("   ") { "${it.category.emoji} ${formatPercent(it.entry.percent)}" },
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(10.dp))
        FilledTonalButton(
            onClick = onCopy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.carry_forward_action))
        }
    }
}

private fun countWord(n: Int): String = when {
    n % 10 == 1 && n % 100 != 11 -> "активный кэшбэк"
    n % 10 in 2..4 && (n % 100 !in 12..14) -> "активных кэшбэка"
    else -> "активных кэшбэков"
}

fun formatPercent(p: Double): String {
    val s = if (p == p.toLong().toDouble()) p.toLong().toString()
            else "%.2f".format(p).trimEnd('0').trimEnd('.', ',')
    return "${s.replace('.', ',')}%"
}
