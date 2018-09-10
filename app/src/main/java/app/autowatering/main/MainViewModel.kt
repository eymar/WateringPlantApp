package app.autowatering.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import app.autowatering.core.WateringClient
import app.autowatering.secondsToDateTimeStr
import app.autowatering.viewmodel.SingleLiveEvent
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class MainViewModel(private val client: WateringClient) : ViewModel(),
        WateringScriptsArrayAdapter.OnWateringScriptActionsListener {

    val scriptsList = MutableLiveData<ArrayList<WateringScriptViewPresentation>>()

    val deviceTimeText = ObservableField<String>()
    val lastSyncTimeText = ObservableField<String>()
    val waterStatusText = ObservableField<String>()

    val progressVisible = ObservableBoolean()

    val toastPub = SingleLiveEvent<String>()

    private val testScript = WateringClient.WateringScript(-1, 1,
            0, System.currentTimeMillis().toInt() / 1000,
            -1, true)

    fun start() {
        sync()
    }

    private fun loadScripts() {
        scriptsList.value = arrayListOf(WateringScriptViewPresentation.map(testScript))

        launch(UI) {
            val scripts = async {
                client.getAllUsualScripts()
            }.await()

            scriptsList.value = scriptsList.value!!.apply {
                addAll(scripts.filter {
                    it.enabled
                }.map {
                    WateringScriptViewPresentation.map(it)
                })
            }
        }
    }

    private fun sync() {
        progressVisible.set(true)
        loadScripts()

        try {
            launch(UI) {
                val setTime = async { client.setTime((System.currentTimeMillis() / 1000).toInt()) }
                val getTime = async { client.getTimeSeconds() }
                val hasWater = async { client.getWaterLevelStatus() }

                waterStatusText.set(if (hasWater.await()) "OK" else "NO")

                val time = getTime.await().secondsToDateTimeStr()
                deviceTimeText.set(getTime.await().secondsToDateTimeStr())
                lastSyncTimeText.set("Синхр-вано: $time")

                if (!setTime.await()) {
                    toastPub.value = "Не удалось синхр-вать текущее время"
                }

                progressVisible.set(false)
            }
        } catch (e: Exception) {
            progressVisible.set(false)
            toastPub.value = e.message ?: "Error :("
        }
    }

    private fun launchTestScript() {
        progressVisible.set(true)
        launch(UI) {
            val job = async {
                client.launchOneShotScript(testScript)
            }

            try {
                val isSuccess = job.await()
                toastPub.value = "Тест ${if (isSuccess) "запущен" else "не запущен"}"
            } catch (e: Exception) {
                toastPub.value = e.message ?: "Error :("
            }

            progressVisible.set(false)
        }
    }

    fun onRefreshClick() {
        sync()
    }

    override fun onScriptActionClick(item: WateringScriptViewPresentation) {
        if (item.isTestScript) {
            launchTestScript()
        } else {
            toastPub.value = "Start edit script"
        }
    }
}