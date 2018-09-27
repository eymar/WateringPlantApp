package app.autowatering.main.edit

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import app.autowatering.R
import kotlinx.android.synthetic.main.edit_watering_script_fragment.*
import java.sql.Time
import java.util.concurrent.TimeUnit
import kotlin.math.min

class EditWateringProgramDialog : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_watering_script_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureView()
    }

    private fun configureView() {
        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        toolbar.inflateMenu(R.menu.edit_watering_program_menu)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit_save -> {
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }

        removeButton.setOnClickListener {

        }

        durationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        intervalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minutes = mapIntervalProgressToMinutes(progress).toLong()
                intervalValueText.text = minutesToTimeString(minutes)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        startInSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minutes = mapIntervalProgressToMinutes(progress).toLong()
                startInlValueText.text = minutesToTimeString(minutes)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun mapIntervalProgressToMinutes(progress: Int): Int {
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

    fun minutesToTimeString(minutes: Long): String {
        val hours = if (minutes >= 60) {
            TimeUnit.MINUTES.toHours(minutes)
        } else {
            0
        }

        val days = if (hours >= 24) {
            TimeUnit.MINUTES.toDays(minutes)
        } else {
            0
        }

        val hoursLeft = hours % 24
        val minutesLeft = minutes % 60

        val str = "%d дн. %d час. %02d мин."

        return str.format(days, hoursLeft, minutesLeft)
    }
}