package app.autowatering.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import app.autowatering.App
import app.autowatering.core.BluetoothConnection
import app.autowatering.main.MainViewModel
import app.autowatering.core.WateringClientImpl
import app.autowatering.main.edit.EditWateringProgramViewModel

class ViewModelFactory(private val app: App) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return with(modelClass) {
            when {
                isAssignableFrom(MainViewModel::class.java) ->
                        createMainViewModel(app.bluetoothConnection!!)

                isAssignableFrom(EditWateringProgramViewModel::class.java) ->
                        createEditWateringProgramViewModel()

                else ->super.create(modelClass)
            }
        } as T
    }

    private fun createMainViewModel(bluetoothConnection: BluetoothConnection): MainViewModel {
        return MainViewModel(WateringClientImpl(bluetoothConnection))
    }

    private fun createEditWateringProgramViewModel():
            EditWateringProgramViewModel {
        return EditWateringProgramViewModel()
    }
}