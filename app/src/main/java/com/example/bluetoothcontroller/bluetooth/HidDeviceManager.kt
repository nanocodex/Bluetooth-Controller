package com.example.bluetoothcontroller.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import java.util.concurrent.Executors

class HidDeviceManager(context: Context) {

    private var hidDevice: BluetoothHidDevice? = null
    private var hostDevice: BluetoothDevice? = null
    private val bluetoothAdapter: BluetoothAdapter? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private val profileServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = proxy as BluetoothHidDevice
                registerApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
            }
        }
    }

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d("HidDeviceManager", "onAppStatusChanged: registered=$registered")
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            Log.d("HidDeviceManager", "onConnectionStateChanged: state=$state")
            if (state == BluetoothProfile.STATE_CONNECTED) {
                hostDevice = device
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                hostDevice = null
            }
        }
    }

    init {
        bluetoothAdapter?.getProfileProxy(context, profileServiceListener, BluetoothProfile.HID_DEVICE)
    }

    @SuppressLint("MissingPermission")
    private fun registerApp() {
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "Android HID",
            "Android HID Emulator",
            "Android",
            BluetoothHidDevice.SUBCLASS1_MOUSE,
            HID_REPORT_DESC
        )
        hidDevice?.registerApp(sdpSettings, null, null, Executors.newSingleThreadExecutor(), callback)
    }

    @SuppressLint("MissingPermission")
    fun sendMouseReport(dx: Int, dy: Int, leftClick: Boolean, rightClick: Boolean) {
        val buttons = (if (leftClick) 0x01 else 0) or (if (rightClick) 0x02 else 0)
        val report = byteArrayOf(
            buttons.toByte(),
            dx.coerceIn(-127, 127).toByte(),
            dy.coerceIn(-127, 127).toByte()
        )
        hostDevice?.let {
            hidDevice?.sendReport(it, 0, report)
        }
    }

    companion object {
        private val HID_REPORT_DESC = byteArrayOf(
            0x05.toByte(), 0x01.toByte(), // USAGE_PAGE (Generic Desktop)
            0x09.toByte(), 0x02.toByte(), // USAGE (Mouse)
            0xa1.toByte(), 0x01.toByte(), // COLLECTION (Application)
            0x09.toByte(), 0x01.toByte(), //   USAGE (Pointer)
            0xa1.toByte(), 0x00.toByte(), //   COLLECTION (Physical)
            0x05.toByte(), 0x09.toByte(), //     USAGE_PAGE (Button)
            0x19.toByte(), 0x01.toByte(), //     USAGE_MINIMUM (Button 1)
            0x29.toByte(), 0x03.toByte(), //     USAGE_MAXIMUM (Button 3)
            0x15.toByte(), 0x00.toByte(), //     LOGICAL_MINIMUM (0)
            0x25.toByte(), 0x01.toByte(), //     LOGICAL_MAXIMUM (1)
            0x95.toByte(), 0x03.toByte(), //     REPORT_COUNT (3)
            0x75.toByte(), 0x01.toByte(), //     REPORT_SIZE (1)
            0x81.toByte(), 0x02.toByte(), //     INPUT (Data,Var,Abs)
            0x95.toByte(), 0x01.toByte(), //     REPORT_COUNT (1)
            0x75.toByte(), 0x05.toByte(), //     REPORT_SIZE (5)
            0x81.toByte(), 0x03.toByte(), //     INPUT (Const,Var,Abs)
            0x05.toByte(), 0x01.toByte(), //     USAGE_PAGE (Generic Desktop)
            0x09.toByte(), 0x30.toByte(), //     USAGE (X)
            0x09.toByte(), 0x31.toByte(), //     USAGE (Y)
            0x15.toByte(), 0x81.toByte(), //     LOGICAL_MINIMUM (-127)
            0x25.toByte(), 0x7f.toByte(), //     LOGICAL_MAXIMUM (127)
            0x75.toByte(), 0x08.toByte(), //     REPORT_SIZE (8)
            0x95.toByte(), 0x02.toByte(), //     REPORT_COUNT (2)
            0x81.toByte(), 0x06.toByte(), //     INPUT (Data,Var,Rel)
            0xc0.toByte(),                //   END_COLLECTION
            0xc0.toByte()                 // END_COLLECTION
        )
    }
}
