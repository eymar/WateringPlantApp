package app.autowatering.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import app.autowatering.App
import app.autowatering.core.BluetoothConnection
import app.autowatering.main.MainViewModel
import app.autowatering.core.WateringClientImpl

class ViewModelFactory(private val app: App) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return with(modelClass) {
            when {
                isAssignableFrom(MainViewModel::class.java) ->
                        createMainViewModel(app.bluetoothConnection!!)

                else ->super.create(modelClass)
            }
        } as T
    }

    private fun createMainViewModel(bluetoothConnection: BluetoothConnection): MainViewModel {
        return MainViewModel(WateringClientImpl(bluetoothConnection))
    }
}