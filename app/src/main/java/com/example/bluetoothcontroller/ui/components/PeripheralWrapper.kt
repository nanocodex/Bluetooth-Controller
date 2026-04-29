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

@Composable
fun PeripheralWrapper(
    title: String,
    onExit: () -> Unit,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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

@Preview(showBackground = true, name = "Peripheral Wrapper (Drawer Closed)")
@Composable
fun PeripheralWrapperPreview() {
    BluetoothControllerTheme {
        PeripheralWrapper(
            title = "Preview Mode",
            onExit = {}
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Touchpad Content Here")
            }
        }
    }
}