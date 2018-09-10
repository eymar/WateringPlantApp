package app.autowatering.main

import app.autowatering.core.WateringClient
import java.util.concurrent.TimeUnit

class WateringScriptViewPresentation {
    var scriptIdText = ""
    var startInText = ""
    var intervalText = ""
    var durationText = ""

    var actionButtonText = ""

    var isTestScript = false

    companion object {
        fun map(item: WateringClient.WateringScript): WateringScriptViewPresentation {
            return WateringScriptViewPresentation().apply {
                isTestScript = item.purposeId == (-1).toByte()

                scriptIdText = if (isTestScript) "Test" else "#${item.purposeId + 1}"

                intervalText = if (isTestScript) {
                    "Без повторений"
                } else {
                    "Интервал: ${item.intervalSeconds} сек."
                }

                actionButtonText = if (isTestScript) {
                    "Выполнить"
                } else {
                    "Редактировать"
                }

                durationText = "Продолжительность: ${item.durationSeconds}"

                var startDiff = item.nextWateringTime - System.currentTimeMillis() / 1000
                if (startDiff < 0) {
                    startDiff = 0
                }
                startInText = "Полив через: ${TimeUnit.SECONDS.toMinutes(startDiff)} мин."

                durationText = "Продолжительность: ${item.durationSeconds} сек."
            }
        }
    }
}