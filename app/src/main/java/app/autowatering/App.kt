package app.autowatering

import android.app.Application
import app.autowatering.core.BluetoothConnection
import app.autowatering.viewmodel.ViewModelFactory

class App : Application() {
    var bluetoothConnection: BluetoothConnection? = null
    val viewModelFactory = ViewModelFactory(this)
}