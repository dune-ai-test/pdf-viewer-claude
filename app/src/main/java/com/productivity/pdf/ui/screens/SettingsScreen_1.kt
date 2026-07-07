package com.productivity.pdf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.productivity.pdf.data.PdfSettingsStore
import com.productivity.pdf.ui.components.TranslucentTopBar

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TranslucentTopBar(title = "Settings", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Page background",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            Text(
                text = "Color shown in the gaps around and between pages while reading.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                PdfSettingsStore.BackgroundPresets.all.forEach { colorInt ->
                    ColorSwatch(
                        colorInt = colorInt,
                        selected = PdfSettingsStore.backgroundColor == colorInt,
                        onClick = { PdfSettingsStore.setBackgroundColor(context, colorInt) },
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Night mode",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Inverts page colors for reading in the dark. This changes how the document's own colors look, not just the app theme.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, end = 8.dp)
                    )
                }
                Switch(
                    checked = PdfSettingsStore.nightMode,
                    onCheckedChange = { PdfSettingsStore.setNightMode(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    colorInt: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(colorInt))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            // Pick a checkmark tint that reads against both light and dark swatches.
            val isDarkSwatch = colorLuminance(colorInt) < 0.5
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = if (isDarkSwatch) Color.White else Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun colorLuminance(colorInt: Int): Double {
    val r = ((colorInt shr 16) and 0xFF) / 255.0
    val g = ((colorInt shr 8) and 0xFF) / 255.0
    val b = (colorInt and 0xFF) / 255.0
    return 0.299 * r + 0.587 * g + 0.114 * b
}
