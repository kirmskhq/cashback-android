package com.mo.cashback.ui.promos

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mo.cashback.R
import com.mo.cashback.ui.component.AppTopBar
import com.mo.cashback.ui.component.appViewModel
import com.mo.cashback.ui.component.bestForegroundOn
import com.mo.cashback.ui.component.parseHex

@Composable
fun PromoDetailScreen(promoId: String, onBack: () -> Unit) {
    val vm = appViewModel(key = "promo_$promoId") { PromoDetailViewModel(it, promoId) }
    val promo by vm.promo.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    val current = promo
    val color = current?.let { parseHex(it.accentHex) } ?: MaterialTheme.colorScheme.primary
    val on = color.bestForegroundOn()

    Scaffold(
        topBar = {
            AppTopBar(
                title = current?.title ?: "",
                onBack = onBack,
                containerColor = color,
                contentColor = on,
            )
        },
    ) { pad ->
        if (current == null) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {}
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // colored header band
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color)
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
            ) {
                Text(
                    text = stringResource(R.string.promos_section_label),
                    color = on.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = current.subtitle,
                    color = on,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            HorizontalDivider(color = Color(0xFFECECEF))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp),
            ) {
                Text(
                    text = stringResource(R.string.promo_terms_header),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = current.benefits,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { uriHandler.openUri(current.url) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color,
                        contentColor = on,
                    ),
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.promo_open_in_browser))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = current.url,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
