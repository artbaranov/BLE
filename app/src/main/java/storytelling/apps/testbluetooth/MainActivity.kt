package storytelling.apps.testbluetooth

import android.Manifest.permission.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import storytelling.apps.testbluetooth.ui.theme.TestBluetoothTheme


class MainActivity : ComponentActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter

    data class UiState(
        var list: List<String?> = emptyList()
    )

    var uiState by mutableStateOf(UiState())
        private set

    private val list1 = mutableListOf<String?>()



    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    checkAndRequestPermission()
                    val deviceName = device?.address
                    list1.add(deviceName.toString())
                    uiState = uiState.copy(list = list1)
                }
                ACTION_DISCOVERY_STARTED -> {
                    Log.d("Device", "Discovery Started")
                }
                ACTION_DISCOVERY_FINISHED -> {
                    Log.d("Device", "Discovery Finished")
                    for (i in uiState.list) {
                        Log.d("Device", i.toString())
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initManagers()
        checkAndRequestPermission()
        checkBluetoothIsEnabled()
        val filter1 = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val filter2 = IntentFilter(ACTION_DISCOVERY_STARTED)
        val filter3 = IntentFilter(ACTION_DISCOVERY_FINISHED)

        registerReceiver(receiver, filter1)
        registerReceiver(receiver, filter2)
        registerReceiver(receiver, filter3)

        setContent {
            TestBluetoothTheme {
                Screen(uiState)
            }
        }
    }

    private fun checkBluetoothIsEnabled() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBlIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            checkAndRequestPermission()
            startActivity(enableBlIntent)
        }
    }

    private fun checkAndRequestPermission() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            this, BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
        if (permissionGranted) return
        requestPermissions(
            arrayOf(
                BLUETOOTH_CONNECT,
                BLUETOOTH_SCAN,
                BLUETOOTH,
                BLUETOOTH_ADMIN,
                ACCESS_COARSE_LOCATION,
                ACCESS_FINE_LOCATION
            ),
            1
        )
    }

    private fun initManagers() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    @Composable
    private fun Screen(uiState: UiState) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items = uiState.list) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .border(1.dp, Color.Black)
                    ) {
                        Text(text = item!!)
                    }
                }
            }
            Button(
                onClick = {
                    checkAndRequestPermission()
                    val discoveryAvailable = bluetoothAdapter.startDiscovery()
                    Log.d("Device", "Is discovery available: $discoveryAvailable")
                }) {
                Text(text = "StartSearch")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)
    }
}

