package com.mo.cashback.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.mo.cashback.ui.component.bestForegroundOn
import com.mo.cashback.ui.component.displayName
import com.mo.cashback.ui.component.monthLabel
import com.mo.cashback.ui.component.parseHex

@Composable
fun HistoryScreen() {
    val vm = appViewModel { HistoryViewModel(it) }
    val ym by vm.yearMonth.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppTopBar(title = stringResource(R.string.nav_history)) },
    ) { pad ->
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            MonthPicker(
                label = monthLabel(ym.year, ym.monthValue),
                onPrev = vm::prev,
                onNext = vm::next,
                onToday = vm::today,
            )
            HorizontalDivider(color = Color(0xFFECECEF))
            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.history_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val grouped = entries.groupBy { it.bank.id }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    grouped.forEach { (_, bankEntries) ->
                        val bank = bankEntries.first().bank
                        val color = parseHex(bank.colorHex)
                        val on = color.bestForegroundOn()
                        item(key = "h_${bank.id}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color)
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                            ) {
                                Text(bank.name, color = on, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        items(bankEntries, key = { it.entry.id }) { row -> HistoryEntryRow(row) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthPicker(label: String, onPrev: () -> Unit, onNext: () -> Unit, onToday: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.prev_month))
        }
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable(onClick = onToday)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                stringResource(R.string.today),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.next_month))
        }
    }
}

@Composable
private fun HistoryEntryRow(row: EntryWithRefs) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(row.category.emoji, fontSize = 20.sp)
        Spacer(Modifier.width(14.dp))
        Text(row.category.displayName(), modifier = Modifier.weight(1f), fontSize = 15.sp)
        Text(
            text = formatPercent(row.entry.percent),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
    }
    HorizontalDivider(color = Color(0xFFECECEF))
}
