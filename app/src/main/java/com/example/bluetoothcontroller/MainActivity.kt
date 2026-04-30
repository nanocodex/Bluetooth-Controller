package com.example.bluetoothcontroller

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bluetoothcontroller.ui.components.PeripheralWrapper
import com.example.bluetoothcontroller.ui.landing.LandingScreen
import com.example.bluetoothcontroller.ui.peripherals.MouseScreen
import com.example.bluetoothcontroller.ui.theme.BluetoothControllerTheme
import com.example.bluetoothcontroller.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluetoothControllerTheme {
                val context = LocalContext.current
                
                // Use rememberSaveable to handle rotation/reconfiguration
                var permissionsGranted by rememberSaveable { mutableStateOf(false) }
                var showDenialMessage by rememberSaveable { mutableStateOf(false) }
                
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    val bluetoothConnectGranted = results[Manifest.permission.BLUETOOTH_CONNECT] == true
                    permissionsGranted = bluetoothConnectGranted
                    showDenialMessage = !bluetoothConnectGranted
                }

                LaunchedEffect(Unit) {
                    // Check if we already have the critical permission
                    val hasConnect = context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    
                    if (hasConnect) {
                        permissionsGranted = true
                    } else {
                        val permissions = mutableListOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_ADVERTISE
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        launcher.launch(permissions.toTypedArray())
                    }
                }

                if (permissionsGranted) {
                    val navController = rememberNavController()
                    val viewModel: MainViewModel = viewModel()

                    NavHost(navController = navController, startDestination = "landing") {
                        composable("landing") {
                            LandingScreen(onSelectMouse = { navController.navigate("mouse") })
                        }
                        composable("mouse") {
                            PeripheralWrapper(
                                title = "Touchpad Mouse",
                                onExit = { navController.popBackStack() }
                            ) {
                                MouseScreen(viewModel = viewModel)
                            }
                        }
                    }
                } else if (showDenialMessage) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bluetooth permissions are required for this app to connect to your PC. Please enable them in your device settings to continue.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    BluetoothControllerTheme {
        LandingScreen(onSelectMouse = {})
    }
}
