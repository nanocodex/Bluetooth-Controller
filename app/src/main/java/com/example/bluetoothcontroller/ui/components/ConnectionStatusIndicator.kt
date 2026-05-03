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
import androidx.compose.ui.tooling.preview.Preview
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
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                StrengthIndicator(
                    strength = status.rxStrength
                )

                Spacer(modifier = Modifier.height(4.dp))

                LatencyIndicator(
                    latency = status.latencyMs
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Note: Only reception (RX) strength is shown. Transmission (TX) metrics are unavailable.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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
    val percent = strength?.let {
        ((it - MIN_RSSI).toFloat() / (MAX_RSSI - MIN_RSSI) * 100).toInt().coerceIn(0, 100)
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Signal Strength",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = percent?.let { "$it%" } ?: "N/A",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { calculateProgress(strength) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {}
        )
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

@Composable
private fun LatencyIndicator(
    latency: Long?
) {
    val color = getLatencyColor(latency)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Link Latency",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
        Text(
            text = latency?.let { "$it ms" } ?: "N/A",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private fun getLatencyColor(latency: Long?): Color {
    return when {
        latency == null -> Color.Gray
        latency < 40 -> Color(0xFF4CAF50) // Green (Ideal)
        latency < 120 -> Color(0xFFFFC107) // Amber (Acceptable)
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

@Preview(showBackground = true, name = "Ideal Connection")
@Composable
fun PreviewConnectionIdeal() {
    MaterialTheme {
        Box(Modifier.padding(16.dp)) {
            ConnectionStatusIndicator(
                status = ConnectionStatus(
                    type = ConnectionType.BLUETOOTH,
                    deviceName = "Windows PC",
                    rxStrength = -40,
                    latencyMs = 10
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "Acceptable Connection")
@Composable
fun PreviewConnectionAcceptable() {
    MaterialTheme {
        Box(Modifier.padding(16.dp)) {
            ConnectionStatusIndicator(
                status = ConnectionStatus(
                    type = ConnectionType.BLUETOOTH,
                    deviceName = "Windows PC",
                    rxStrength = -65,
                    latencyMs = 40
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "Poor Connection")
@Composable
fun PreviewConnectionPoor() {
    MaterialTheme {
        Box(Modifier.padding(16.dp)) {
            ConnectionStatusIndicator(
                status = ConnectionStatus(
                    type = ConnectionType.BLUETOOTH,
                    deviceName = "Windows PC",
                    rxStrength = -80,
                    latencyMs = 120
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "Unknown State")
@Composable
fun PreviewConnectionUnknown() {
    MaterialTheme {
        Box(Modifier.padding(16.dp)) {
            ConnectionStatusIndicator(
                status = ConnectionStatus(
                    type = ConnectionType.BLUETOOTH,
                    deviceName = "Unknown Device",
                    rxStrength = null,
                    latencyMs = null
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "Disconnected")
@Composable
fun PreviewConnectionDisconnected() {
    MaterialTheme {
        Box(Modifier.padding(16.dp)) {
            ConnectionStatusIndicator(
                status = ConnectionStatus(
                    type = ConnectionType.DISCONNECTED
                )
            )
        }
    }
}
