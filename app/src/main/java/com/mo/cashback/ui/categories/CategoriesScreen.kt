package com.mo.cashback.ui.categories

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import com.mo.cashback.data.CategoryTopOffer
import com.mo.cashback.ui.banks.formatPercent
import com.mo.cashback.ui.component.AppTopBar
import com.mo.cashback.ui.component.appViewModel
import com.mo.cashback.ui.component.bestForegroundOn
import com.mo.cashback.ui.component.displayName
import com.mo.cashback.ui.component.parseHex

@Composable
fun CategoriesScreen(onCategoryClick: (String) -> Unit) {
    val vm = appViewModel { CategoriesViewModel(it) }
    val items by vm.items.collectAsStateWithLifecycle()

    val active = items.filter { it.topBank != null }
    val inactive = items.filter { it.topBank == null }

    Scaffold(
        topBar = { AppTopBar(title = stringResource(R.string.nav_categories)) },
    ) { pad ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(active, key = { "a_${it.category.id}" }) { offer ->
                ActiveChip(offer, onClick = { onCategoryClick(offer.category.id) })
            }
            if (active.isNotEmpty() && inactive.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    SectionDivider(stringResource(R.string.cat_section_inactive))
                }
            }
            items(inactive, key = { "i_${it.category.id}" }) { offer ->
                InactiveChip(offer, onClick = { onCategoryClick(offer.category.id) })
            }
        }
    }
}

@Composable
private fun SectionDivider(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "  $label  ",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ActiveChip(offer: CategoryTopOffer, onClick: () -> Unit) {
    val bank = offer.topBank!!
    val color = parseHex(bank.colorHex)
    val on = color.bestForegroundOn()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        // top-right +N pill
        if (offer.bankCount > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(8.dp))
                    .background(on.copy(alpha = 0.18f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    "+${offer.bankCount - 1}",
                    color = on,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        // category row (top)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Text(offer.category.emoji, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = offer.category.displayName(),
                color = on,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )
        }
        // bank + % (bottom)
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = bank.name,
                color = on.copy(alpha = 0.92f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = formatPercent(offer.topPercent ?: 0.0),
                color = on,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun InactiveChip(offer: CategoryTopOffer, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(offer.category.emoji, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = offer.category.displayName(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}
