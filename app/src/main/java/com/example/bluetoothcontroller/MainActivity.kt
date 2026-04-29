package com.example.bluetoothcontroller

import android.os.Bundle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.bluetoothcontroller.ui.theme.BluetoothControllerTheme
import com.example.bluetoothcontroller.ui.landing.LandingScreen
import com.example.bluetoothcontroller.ui.peripherals.MouseScreen
import com.example.bluetoothcontroller.ui.components.PeripheralWrapper
import com.example.bluetoothcontroller.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluetoothControllerTheme {
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
