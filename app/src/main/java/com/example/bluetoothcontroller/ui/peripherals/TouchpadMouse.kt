package com.example.bluetoothcontroller.ui.peripherals

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.example.bluetoothcontroller.viewmodel.MainViewModel
import com.example.bluetoothcontroller.ui.theme.BluetoothControllerTheme

@Composable
fun MouseScreen(viewModel: MainViewModel? = null) {
    // 1. The Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            // 2. Handling Taps (Clicks)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // Send Left Click HID Report
                        viewModel?.sendMouseReport(0, 0, leftClick = true, rightClick = false)
                        // Immediately release the button
                        viewModel?.sendMouseReport(0, 0, leftClick = false, rightClick = false)
                    },
                    onLongPress = {
                        // Send Right Click HID Report
                        viewModel?.sendMouseReport(0, 0, leftClick = false, rightClick = true)
                        // Immediately release the button
                        viewModel?.sendMouseReport(0, 0, leftClick = false, rightClick = false)
                    }
                )
            }
            // 3. Handling Movement (Mouse Move)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()

                        if (change.pressed) {
                            val dragAmount = change.position - change.previousPosition

                            // 4. Check if the finger actually moved
                            if (dragAmount.x != 0f || dragAmount.y != 0f) {
                                // Send Movement HID Report
                                viewModel?.sendMouseReport(
                                    dragAmount.x.toInt(),
                                    dragAmount.y.toInt(),
                                    leftClick = false,
                                    rightClick = false
                                )
                                change.consume()
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // 5. Visual Feedback
        Text(
            text = "Touchpad Area",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Preview(showBackground = true, name = "Mouse Touchpad Preview")
@Composable
fun MouseScreenPreview() {
    BluetoothControllerTheme {
        MouseScreen()
    }
}
