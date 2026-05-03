package com.example.bluetoothcontroller.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import com.example.bluetoothcontroller.ui.theme.BluetoothControllerTheme
import com.example.bluetoothcontroller.bluetooth.ConnectionStatus
import com.example.bluetoothcontroller.bluetooth.ConnectionType

@Composable
fun PeripheralWrapper(
    title: String,
    status: ConnectionStatus = ConnectionStatus(),
    onExit: () -> Unit,
    onDrawerStateChanged: (Boolean) -> Unit = {},
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Control polling based on drawer state
    LaunchedEffect(drawerState.isOpen) {
        onDrawerStateChanged(drawerState.isOpen)
    }

    // GESTURE-ONLY EXIT LOGIC
    // This intercepts the system back gesture (swipe from edge).
    // Instead of closing the app/screen, it simply opens the navigation drawer.
    BackHandler(enabled = true) {
        scope.launch {
            drawerState.open()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                
                ConnectionStatusIndicator(status = status)

                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Exit to Menu") },
                    selected = false,
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onExit()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                // You can add "Settings" or "Macro" shortcuts here later
            }
        }
    ) {
        // This is the actual UI of the peripheral (e.g., the Touchpad)
        // It fills the whole screen under the drawer logic
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Preview(showBackground = true, name = "BT Connected - Ideal")
@Composable
fun PeripheralWrapperIdealPreview() {
    BluetoothControllerTheme {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
        PeripheralWrapper(
            title = "Preview Mode",
            status = ConnectionStatus(
                type = ConnectionType.BLUETOOTH,
                deviceName = "Windows PC",
                rxStrength = -40,
                latencyMs = 10
            ),
            onExit = {},
            drawerState = drawerState
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Peripheral UI Here")
            }
        }
    }
}

@Preview(showBackground = true, name = "BT Connected - Acceptable")
@Composable
fun PeripheralWrapperAcceptablePreview() {
    BluetoothControllerTheme {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
        PeripheralWrapper(
            title = "Preview Mode",
            status = ConnectionStatus(
                type = ConnectionType.BLUETOOTH,
                deviceName = "Windows PC",
                rxStrength = -60,
                latencyMs = 40
            ),
            onExit = {},
            drawerState = drawerState
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Peripheral UI Here")
            }
        }
    }
}

@Preview(showBackground = true, name = "BT Connected - Poor")
@Composable
fun PeripheralWrapperPoorPreview() {
    BluetoothControllerTheme {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
        PeripheralWrapper(
            title = "Preview Mode",
            status = ConnectionStatus(
                type = ConnectionType.BLUETOOTH,
                deviceName = "Windows PC",
                rxStrength = -80,
                latencyMs = 120
            ),
            onExit = {},
            drawerState = drawerState
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Peripheral UI Here")
            }
        }
    }
}

@Preview(showBackground = true, name = "BT Connected - Unknown")
@Composable
fun PeripheralWrapperUnknownPreview() {
    BluetoothControllerTheme {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
        PeripheralWrapper(
            title = "Preview Mode",
            status = ConnectionStatus(
                type = ConnectionType.BLUETOOTH,
                deviceName = "Windows PC",
                rxStrength = null,
                latencyMs = null
            ),
            onExit = {},
            drawerState = drawerState
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Peripheral UI Here")
            }
        }
    }
}

@Preview(showBackground = true, name = "Disconnected")
@Composable
fun PeripheralWrapperDisconnectedPreview() {
    BluetoothControllerTheme {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
        PeripheralWrapper(
            title = "Preview Mode",
            status = ConnectionStatus(type = ConnectionType.DISCONNECTED),
            onExit = {},
            drawerState = drawerState
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Peripheral UI Here")
            }
        }
    }
}

@Preview(showBackground = true, name = "Peripheral Wrapper (Drawer Closed)")
@Composable
fun PeripheralWrapperClosedPreview() {
    BluetoothControllerTheme {
        PeripheralWrapper(
            title = "Preview Mode",
            onExit = {}
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Peripheral UI Here")
            }
        }
    }
}
