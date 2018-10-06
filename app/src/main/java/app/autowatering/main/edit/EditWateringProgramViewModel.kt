package app.autowatering.main.edit

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import java.util.concurrent.TimeUnit

class EditWateringProgramViewModel : ViewModel() {

    val durationValueText = ObservableField<String>()
    val intervalValueText = ObservableField<String>()
    val startInValueText = ObservableField<String>()

    fun start() {

    }

    /**
     * @param value - int in range from 0 to 100
     */
    fun onWateringDurationChanged(value: Int) {
        val seconds = mapDurationValueToSeconds(value)
        durationValueText.set("%d сек.".format(seconds))
    }

    /**
     * @param value - int in range from 0 to 100
     */
    fun onWateringIntervalChanged(value: Int) {
        val minutes = mapIntervalValueToMinutes(value)
        intervalValueText.set(minutesToTimeString(minutes))
    }

    /**
     * @param value - int in range from 0 to 100
     */
    fun onStartInChanged(value: Int) {
        val minutes = mapStartInValueToMinutes(value)
        startInValueText.set(minutesToTimeString(minutes))
    }

    /**
     * Maps int value of Interval in range of [0, 100] to minutes (not lineary)
     */
    private fun mapIntervalValueToMinutes(progress: Int): Int {
        if (progress <= 9) {
            return progress + 1
        }

        if (progress <= 80) {
            val p = progress - 9
            return p * 30
        }

        val p = progress - 79

        return p * 24 * 60
    }

    /**
     * Maps int value of StartIn in range of [0, 100] to minutes (not lineary)
     */
    private fun mapStartInValueToMinutes(progress: Int): Int {
        if (progress <= 9) {
            return progress + 1
        }

        if (progress <= 80) {
            val p = progress - 9
            return p * 30
        }

        val p = progress - 79

        return p * 24 * 60
    }

    /**
     * Maps int value of Duration in range of [0, 100] to seconds
     */
    private fun mapDurationValueToSeconds(progress: Int): Int {
        return (progress + 1) * 2
    }

    private fun minutesToTimeString(minutes: Int): String {
        val minutesLong = minutes.toLong()

        val hours = if (minutes >= 60) {
            TimeUnit.MINUTES.toHours(minutesLong)
        } else {
            0
        }

        val days = if (hours >= 24) {
            TimeUnit.MINUTES.toDays(minutesLong)
        } else {
            0
        }

        val hoursLeft = hours % 24
        val minutesLeft = minutes % 60

        return "%d дн. %d час. %02d мин.".format(days, hoursLeft, minutesLeft)
    }
}