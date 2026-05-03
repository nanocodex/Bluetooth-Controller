package com.example.bluetoothcontroller.bluetooth

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

enum class ConnectionType {
    BLUETOOTH, DISCONNECTED, WIRED
}

data class ConnectionStatus(
    val type: ConnectionType = ConnectionType.DISCONNECTED,
    val deviceName: String? = null,
    val rxStrength: Int? = null,
    val latencyMs: Long? = null
)

class HidDeviceService : Service() {

    private var bluetoothHidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val binder = LocalBinder()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus())
    val connectionStatus = _connectionStatus.asStateFlow()

    private var isPolling = false
    private var lastRssiRequestTime: Long = 0

    private val handler = Handler(Looper.getMainLooper())
    private val rssiPoller = object : Runnable {
        @SuppressLint("MissingPermission")
        override fun run() {
            if (isPolling && connectedDevice != null && bluetoothGatt != null) {
                try {
                    lastRssiRequestTime = System.currentTimeMillis()
                    bluetoothGatt?.readRemoteRssi()
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading RSSI", e)
                }
                handler.postDelayed(this, 2000) // Poll every 2 seconds
            }
        }
    }

    fun setPolling(enabled: Boolean) {
        if (isPolling == enabled) return
        isPolling = enabled
        if (enabled) {
            handler.post(rssiPoller)
        } else {
            handler.removeCallbacks(rssiPoller)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "GATT connected for RSSI polling")
                gatt?.readRemoteRssi()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "GATT disconnected")
                bluetoothGatt = null
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val latency = System.currentTimeMillis() - lastRssiRequestTime
                updateRssiAndLatency(rssi, latency)
            }
        }
    }

    private fun updateRssiAndLatency(rssi: Int, latency: Long) {
        val current = _connectionStatus.value
        if (current.type == ConnectionType.BLUETOOTH) {
            _connectionStatus.value = current.copy(
                rxStrength = rssi,
                latencyMs = latency
            )
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): HidDeviceService = this@HidDeviceService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private val profileServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                bluetoothHidDevice = proxy as BluetoothHidDevice
                registerApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                bluetoothHidDevice = null
            }
        }
    }

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d(TAG, "HID App registered: $registered")
        }

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            if (state == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = device
                _connectionStatus.value = ConnectionStatus(
                    type = ConnectionType.BLUETOOTH,
                    deviceName = device?.name ?: "Unknown Device",
                    rxStrength = null
                )
                
                // Start RSSI polling via GATT
                bluetoothGatt = device?.connectGatt(this@HidDeviceService, false, gattCallback)
                if (isPolling) {
                    handler.post(rssiPoller)
                }
                
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                handler.removeCallbacks(rssiPoller)
                bluetoothGatt?.close()
                bluetoothGatt = null
                connectedDevice = null
                _connectionStatus.value = ConnectionStatus(type = ConnectionType.DISCONNECTED)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        adapter?.getProfileProxy(this, profileServiceListener, BluetoothProfile.HID_DEVICE)
    }

    private fun startAsForeground() {
        val channelId = "hid_service_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Bluetooth HID Service",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Mouse Controller Active")
            .setContentText("Connected and ready to send mouse reports.")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
    }

    @SuppressLint("MissingPermission")
    private fun registerApp() {
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "Android Mouse",
            "Mobile Bluetooth HID",
            "Android",
            BluetoothHidDevice.SUBCLASS1_MOUSE,
            MOUSE_REPORT_DESCRIPTOR
        )
        bluetoothHidDevice?.registerApp(sdpSettings, null, null, Executors.newSingleThreadExecutor(), hidCallback)
    }

    @SuppressLint("MissingPermission")
    fun sendMouseData(left: Boolean, right: Boolean, x: Float, y: Float) {
        val device = connectedDevice ?: return
        val report = MouseReportParser.createReport(left, right, false, x, y)
        bluetoothHidDevice?.sendReport(device, 0, report)
    }

    companion object {
        private const val TAG = "HidDeviceService"
        private const val NOTIFICATION_ID = 1

        private val MOUSE_REPORT_DESCRIPTOR = byteArrayOf(
            0x05.toByte(), 0x01.toByte(),         // Usage Page (Generic Desktop)
            0x09.toByte(), 0x02.toByte(),         // Usage (Mouse)
            0xA1.toByte(), 0x01.toByte(),         // Collection (Application)
            0x09.toByte(), 0x01.toByte(),         // Usage (Pointer)
            0xA1.toByte(), 0x00.toByte(),         // Collection (Physical)
            0x05.toByte(), 0x09.toByte(),         // Usage Page (Buttons)
            0x19.toByte(), 0x01.toByte(),         // Usage Minimum (1)
            0x29.toByte(), 0x03.toByte(),         // Usage Maximum (3)
            0x15.toByte(), 0x00.toByte(),         // Logical Minimum (0)
            0x25.toByte(), 0x01.toByte(),         // Logical Maximum (1)
            0x95.toByte(), 0x03.toByte(),         // Report Count (3)
            0x75.toByte(), 0x01.toByte(),         // Report Size (1)
            0x81.toByte(), 0x02.toByte(),         // Input (Data, Variable, Absolute)
            0x95.toByte(), 0x01.toByte(),         // Report Count (1)
            0x75.toByte(), 0x05.toByte(),         // Report Size (5)
            0x81.toByte(), 0x01.toByte(),         // Input (Constant)
            0x05.toByte(), 0x01.toByte(),         // Usage Page (Generic Desktop)
            0x09.toByte(), 0x30.toByte(),         // Usage (X)
            0x09.toByte(), 0x31.toByte(),         // Usage (Y)
            0x15.toByte(), 0x81.toByte(),         // Logical Minimum (-127)
            0x25.toByte(), 0x7F.toByte(),         // Logical Maximum (127)
            0x75.toByte(), 0x08.toByte(),         // Report Size (8)
            0x95.toByte(), 0x02.toByte(),         // Report Count (2)
            0x81.toByte(), 0x06.toByte(),         // Input (Data, Variable, Relative)
            0xC0.toByte(), 0xC0.toByte()          // End Collection
        )
    }
}
