package com.mo.cashback.ui.component

import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mo.cashback.data.Bank
import com.mo.cashback.data.Category
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

fun parseHex(hex: String): Color = Color(AndroidColor.parseColor(hex))

fun Color.bestForegroundOn(): Color =
    if (luminance() > 0.55f) Color(0xFF1A1A1D) else Color.White

@Composable
fun Category.displayName(): String {
    val key = nameResKey
    if (isBuiltIn && key != null) {
        val ctx = LocalContext.current
        val id = ctx.resources.getIdentifier(key, "string", ctx.packageName)
        if (id != 0) return stringResource(id)
    }
    return name
}

fun monthLabel(year: Int, month: Int): String {
    val m = Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        .replaceFirstChar { it.titlecase(Locale("ru")) }
    return "$m $year"
}

fun currentYearMonth(): Pair<Int, Int> {
    val ym = YearMonth.now()
    return ym.year to ym.monthValue
}

fun bankColor(bank: Bank): Color = parseHex(bank.colorHex)
