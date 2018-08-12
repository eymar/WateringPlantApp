package app.autowatering

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import android.bluetooth.BluetoothSocket
import android.widget.Toast
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.io.IOException
import java.util.Date
import java.util.UUID


class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_ENABLE_BT = 334
    }

    private val btAdapter: BluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val btDevicesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address

                Log.d("BT-Device", "$deviceName $deviceHardwareAddress")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()

            launch(UI) {
                if (i == 0) {
                    i++

                    val currTime = Date()
                    val result = async {
                        wateringClient!!.setTime((currTime.time / 1000).toInt())
                    }.await()


                    Toast.makeText(this@MainActivity,
                            "Set time: $currTime", Toast.LENGTH_SHORT).show()


                } else {
                    val result = async {
                        wateringClient!!.getTimeSeconds()
                    }.await()

                    Toast.makeText(this@MainActivity,
                            "Get time: ${Date(result * 1000L)}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (!btAdapter.isEnabled) {
            askToEnableBt()
        } else {
            onBtEnabled()
        }
    }

    private var i = 0

    private var bluetoothConnection: BluetoothConnection? = null
    private var wateringClient: WateringClient? = null

    private fun onBtEnabled() {
        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(btDevicesReceiver, filter)

//        btAdapter.startDiscovery()

        val pairedDevices = btAdapter.getBondedDevices()

        if (pairedDevices.size > 0) {

            var deviceOur: BluetoothDevice? = null
            // There are paired devices. Get the name and address of each paired device.
            for (device in pairedDevices) {
                val deviceName = device.getName()
                val deviceHardwareAddress = device.getAddress() // MAC address

                Log.d("BT-Device-p", "$deviceName $deviceHardwareAddress")

                if (deviceHardwareAddress == "00:18:EF:00:1C:25") {
                    deviceOur = device

                    bluetoothConnection = BluetoothConnection(deviceOur)
                    launch(UI) {
                        async {
                            bluetoothConnection!!.connect()
                            wateringClient = WateringClientImpl(bluetoothConnection!!)
                        }.await()
                    }
                }
            }

//            if (deviceOur != null) {
//                val t = ConnectThread(deviceOur)
//                t.start()
//            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                onBtEnabled()
            }
        }
    }

    private fun askToEnableBt() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(btDevicesReceiver)
        super.onDestroy()
    }

    private inner class ConnectThread(private val mmDevice: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket?

        init {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            var tmp: BluetoothSocket? = null

            try {
                val uid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = mmDevice.createRfcommSocketToServiceRecord(uid)
            } catch (e: IOException) {
                Log.e("Connect", "Socket's create() method failed", e)
            }

            mmSocket = tmp
        }

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            btAdapter.cancelDiscovery()

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket!!.connect()
            } catch (connectException: IOException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket!!.close()
                } catch (closeException: IOException) {
                    Log.e("Connect", "Could not close the client socket", closeException)
                }

                return
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
//            manageMyConnectedSocket(mmSocket)

            val i = 10
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e("Connect", "Could not close the client socket", e)
            }

        }
    }
}
