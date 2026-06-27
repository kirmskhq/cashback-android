package com.mo.cashback.ui.banks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mo.cashback.R
import com.mo.cashback.data.BankWithCount
import com.mo.cashback.ui.component.AppTopBar
import com.mo.cashback.ui.component.appViewModel
import com.mo.cashback.ui.component.bankColor
import com.mo.cashback.ui.component.bestForegroundOn

@Composable
fun BanksScreen(onBankClick: (String) -> Unit, onOpenPicker: () -> Unit) {
    val vm = appViewModel { BanksViewModel(it) }
    val banks by vm.banks.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.nav_banks),
                actions = {
                    if (banks.isNotEmpty()) {
                        IconButton(onClick = onOpenPicker) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.banks_edit_action))
                        }
                    }
                },
            )
        },
    ) { pad ->
        if (banks.isEmpty()) {
            EmptyState(onAddClick = onOpenPicker, modifier = Modifier.padding(pad).fillMaxSize())
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(banks, key = { it.bank.id }) { bwc ->
                    BankCard(bwc, onClick = { onBankClick(bwc.bank.id) })
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Text("🏦", fontSize = 56.sp)
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.banks_empty_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.banks_empty_subtitle),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            Button(onClick = onAddClick) {
                Text(stringResource(R.string.banks_empty_cta))
            }
        }
    }
}

@Composable
private fun BankCard(bwc: BankWithCount, onClick: () -> Unit) {
    val color = bankColor(bwc.bank)
    val on = color.bestForegroundOn()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Text(
            text = countLabel(bwc.activeCount),
            color = on.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.TopStart),
        )
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = bwc.bank.name,
                color = on,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun countLabel(n: Int): String {
    if (n == 0) return stringResource(R.string.cashback_count_none)
    val res = when {
        n % 10 == 1 && n % 100 != 11 -> R.string.cashback_count_one
        n % 10 in 2..4 && (n % 100 !in 12..14) -> R.string.cashback_count_few
        else -> R.string.cashback_count_many
    }
    return stringResource(res, n)
}
