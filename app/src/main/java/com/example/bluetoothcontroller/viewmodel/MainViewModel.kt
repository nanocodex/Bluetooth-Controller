package com.example.bluetoothcontroller.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothcontroller.bluetooth.ConnectionStatus
import com.example.bluetoothcontroller.bluetooth.HidDeviceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private var hidService: HidDeviceService? = null
    private var isBound = false

    private val _connectionStatus = MutableStateFlow(ConnectionStatus())
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as HidDeviceService.LocalBinder
            val boundService = binder.getService()
            hidService = boundService
            isBound = true

            // Observe connection status from service
            viewModelScope.launch {
                boundService.connectionStatus.collect { status ->
                    _connectionStatus.value = status
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            hidService = null
            isBound = false
        }
    }

    init {
        Intent(application, HidDeviceService::class.java).also { intent ->
            application.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun sendMouseReport(dx: Int, dy: Int, leftClick: Boolean, rightClick: Boolean) {
        // We use the service to send data if it's bound
        hidService?.sendMouseData(
            left = leftClick,
            right = rightClick,
            x = dx.toFloat(),
            y = dy.toFloat()
        )
    }

    fun setPolling(enabled: Boolean) {
        hidService?.setPolling(enabled)
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            getApplication<Application>().unbindService(connection)
            isBound = false
        }
    }
}
