package app.autowatering.main.edit

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import app.autowatering.core.WateringClient
import app.autowatering.viewmodel.SingleLiveEvent
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class EditWateringProgramViewModel : ViewModel() {

    private var client: WateringClient? = null

    private var script: WateringClient.WateringScript? = null

    val screenTitle = ObservableField<String>()
    val isEditScript = ObservableBoolean()
    val inProgress = ObservableBoolean()
    val finishCommand = SingleLiveEvent<Unit>()


    fun start(scriptToEdit: WateringClient.WateringScript? = null) {
        if (scriptToEdit == null) {
            script = WateringClient.WateringScript(60, 10,
                    0, 0, -1, true)
            configureViewForCreateScript()
        } else {
            script = scriptToEdit
            configureViewForEditScript()
        }
    }

    private fun configureViewForEditScript() {
        screenTitle.set("Новый скрипт")
        isEditScript.set(true)
    }

    private fun configureViewForCreateScript() {
        screenTitle.set("Редактировать")
        isEditScript.set(false)
    }

    fun onSaveClicked() = launch(UI) {
        inProgress.set(true)

        val success = async {
            client!!.commitUsualScript(script!!)
        }.await()


        inProgress.set(false)

        if (success) {
            finishCommand.call()
        }
    }

    fun onDeleteClicked() = launch(UI) {
        inProgress.set(true)

        val success = async {
            client!!.commitUsualScript(script!!.copy(enabled = false))
        }.await()

        inProgress.set(false)

        if (success) {
            finishCommand.call()
        }
    }
}