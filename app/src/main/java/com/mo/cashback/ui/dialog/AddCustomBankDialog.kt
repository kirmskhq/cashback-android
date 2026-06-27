package com.mo.cashback.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.mo.cashback.R
import com.mo.cashback.ui.component.bestForegroundOn
import com.mo.cashback.ui.component.parseHex

private val palette = listOf(
    "#FF1493", // hot pink
    "#BC237B", // magenta
    "#8B3FFD", // purple
    "#3F51B5", // indigo
    "#00BCD4", // cyan
    "#009688", // teal
    "#4CAF50", // green
    "#8BC34A", // light green
    "#FFC107", // amber
    "#FF9800", // orange
    "#795548", // brown
    "#607D8B", // blue-grey
)

@Composable
fun AddCustomBankDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, colorHex: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(palette.first()) }
    val canCreate = name.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_bank_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text(stringResource(R.string.custom_bank_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.custom_bank_color_label),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ColorGrid(selected = color, onPick = { color = it })
                // live preview
                PreviewCard(name = name.ifBlank { stringResource(R.string.custom_bank_preview_placeholder) }, hex = color)
            }
        },
        confirmButton = {
            TextButton(onClick = { if (canCreate) onCreate(name.trim(), color) }, enabled = canCreate) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun ColorGrid(selected: String, onPick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        palette.chunked(6).forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowColors.forEach { hex ->
                    val isSelected = hex == selected
                    val color = parseHex(hex)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color(0x22000000),
                                shape = RoundedCornerShape(10.dp),
                            )
                            .clickable { onPick(hex) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = color.bestForegroundOn(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewCard(name: String, hex: String) {
    val color = parseHex(hex)
    val on = color.bestForegroundOn()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = name,
            color = on,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
