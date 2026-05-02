package com.example.bluetoothcontroller.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.bluetoothcontroller.bluetooth.ConnectionStatus
import com.example.bluetoothcontroller.bluetooth.ConnectionType

private const val MIN_RSSI = -100
private const val MAX_RSSI = 0

@Composable
fun ConnectionStatusIndicator(
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = getIconForType(status.type),
                    contentDescription = null,
                    tint = getColorForType(status.type)
                )
                Text(
                    text = status.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (status.type != ConnectionType.DISCONNECTED) {
                status.deviceName?.let {
                    Text(
                        text = "Connected to: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                StrengthIndicator(
                    strength = status.rxStrength
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Note: Live Transmission (TX) strength is not directly available on Android for this connection type. To measure TX, you can use specialized Bluetooth diagnostics tools on the receiving device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = TextUnit.Unspecified
                )
            } else {
                Text(
                    text = "No device connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun StrengthIndicator(
    strength: Int?
) {
    val color = getStrengthColor(strength)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Received (RX)", style = MaterialTheme.typography.bodySmall)
            Text(
                text = strength?.let { "$it dBm" } ?: "N/A",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { calculateProgress(strength) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(top = 4.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {}
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Poor ($MIN_RSSI)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = "Strong ($MAX_RSSI)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

private fun calculateProgress(strength: Int?): Float {
    if (strength == null) return 0f
    val range = MAX_RSSI - MIN_RSSI
    val normalized = (strength - MIN_RSSI).coerceIn(0, range)
    return normalized.toFloat() / range
}

private fun getStrengthColor(strength: Int?): Color {
    return when {
        strength == null -> Color.Gray
        strength >= -50 -> Color(0xFF4CAF50) // Green (Ideal)
        strength >= -70 -> Color(0xFFFFC107) // Amber (Acceptable)
        else -> Color(0xFFF44336) // Red (Poor)
    }
}

private fun getIconForType(type: ConnectionType): ImageVector {
    return when (type) {
        ConnectionType.BLUETOOTH -> Icons.Default.Bluetooth
        ConnectionType.WIRED -> Icons.Default.Usb
        ConnectionType.DISCONNECTED -> Icons.Default.BluetoothDisabled
    }
}

private fun getColorForType(type: ConnectionType): Color {
    return when (type) {
        ConnectionType.BLUETOOTH -> Color(0xFF2196F3)
        ConnectionType.WIRED -> Color(0xFF9C27B0)
        ConnectionType.DISCONNECTED -> Color.Gray
    }
}
