package app.autowatering.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import app.autowatering.App
import app.autowatering.core.BluetoothConnection
import app.autowatering.main.MainActivity
import app.autowatering.R
import app.autowatering.showToast
import kotlinx.android.synthetic.main.search_device_activity.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class SearchDeviceActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_ENABLE_BT = 334
    }

    private val btAdapter: BluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private val arrayAdapter = BtDevicesArrayAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_device_activity)
        configureView()
    }

    private fun configureView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = arrayAdapter

        arrayAdapter.listener = object : BtDevicesArrayAdapter.BtDeviceItemActionsListener {
            override fun onBtDeviceItemSelected(item: BtDeviceViewPresentation) {
                btAdapter.bondedDevices.firstOrNull { it.address == item.mac }?.run {
                    onBtDeviceSelected(this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (btAdapter.isEnabled) {
            onBtEnabled()
        } else {
            requestEnableBt()
        }
    }

    private fun requestEnableBt() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    private fun onBtDeviceSelected(bluetoothDevice: BluetoothDevice) {
        showToast("Подключаемся...")

        (application as App).run {
            bluetoothConnection = BluetoothConnection(bluetoothDevice)
            launch(UI) {
                try {
                    async {
                        bluetoothConnection!!.connect()
                    }.await()
                    MainActivity.start(this@SearchDeviceActivity)
                    finish()
                } catch (e: Exception) {
                    showToast("Не удалось установить соединение :(")
                }
            }
        }
    }

    private fun onBtEnabled() {
        btAdapter.bondedDevices.mapTo(arrayListOf()) {
            BtDeviceViewPresentation().apply {
                mac = it.address
                name = it.name
            }
        }.run {
            arrayAdapter.update(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MainActivity.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                onBtEnabled()
            }
        }
    }
}
