package com.mo.cashback.ui.promos

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mo.cashback.R
import com.mo.cashback.data.Promo
import com.mo.cashback.ui.component.AppTopBar
import com.mo.cashback.ui.component.appViewModel
import com.mo.cashback.ui.component.bestForegroundOn
import com.mo.cashback.ui.component.parseHex

@Composable
fun PromosScreen(onPromoClick: (String) -> Unit) {
    val vm = appViewModel { PromosViewModel(it) }
    val promos by vm.items.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppTopBar(title = stringResource(R.string.nav_promos)) },
    ) { pad ->
        if (promos.isEmpty()) {
            Box(
                modifier = Modifier.padding(pad).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.promos_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Text(
                        stringResource(R.string.promos_header_hint),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                items(promos, key = { it.id }) { promo ->
                    PromoCard(promo, onClick = { onPromoClick(promo.id) })
                }
            }
        }
    }
}

@Composable
private fun PromoCard(promo: Promo, onClick: () -> Unit) {
    val color = parseHex(promo.accentHex)
    val on = color.bestForegroundOn()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(18.dp),
    ) {
        Text(
            text = promo.title,
            color = on,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = promo.subtitle,
            color = on.copy(alpha = 0.9f),
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.promo_details_cta),
                color = on,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = on)
        }
    }
}
