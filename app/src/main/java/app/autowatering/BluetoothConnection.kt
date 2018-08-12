package app.autowatering

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.experimental.launch
import java.io.IOException
import java.util.UUID

class BluetoothConnection(val bluetoothDevice: BluetoothDevice): RemoteClient {

    private var socket: BluetoothSocket? = null
    private var remoteClient: RemoteClientImpl? = null

    @Throws(IOException::class)
    fun connect() {
        val uid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        socket = bluetoothDevice.createRfcommSocketToServiceRecord(uid)

        try {
            socket!!.connect()
        } catch (e: IOException) {
            socket!!.close()
            throw e
        }

        remoteClient = RemoteClientImpl(RemoteClientImpl.Configuration(
                socket!!.inputStream, socket!!.outputStream,
                5000, 5000, 64, 20))

        launch {
            (remoteClient as RemoteClientImpl).start()
        }
    }

    override suspend fun sendCommand(command: ByteArray): ByteArray {
        return remoteClient!!.sendCommand(command)
    }

}