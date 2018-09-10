package app.autowatering.core

import java.nio.ByteBuffer

interface WateringClient {

    suspend fun setTime(timestampSeconds: Int): Boolean

    suspend fun getTimeSeconds(): Int

    suspend fun getWaterLevelStatus(): Boolean

    suspend fun launchOneShotScript(script: WateringScript): Boolean

    suspend fun commitUsualScript(script: WateringScript): Boolean

    suspend fun getAllUsualScripts(): Array<WateringScript>

    data class WateringScript(val intervalSeconds: Int,
                              val durationSeconds: Short,
                              val lastWateringTime: Int, // timestamp in seconds
                              val nextWateringTime: Int, // timestamp in seconds
                              val purposeId: Byte,
                              val enabled: Boolean) {

        companion object {
            /**
             * Int - 4 bytes,
             * Short - 2 bytes,
             * Byte - 1 byte,
             * Boolean - 1 byte
             */
            const val BYTES_SIZE = (Integer.SIZE / 8) * 3 +
                    java.lang.Short.SIZE / 8 +
                    java.lang.Byte.SIZE / 8 +
                    1 // for boolean

            fun fromBytes(bytes: ByteArray): WateringScript {
                val buffer = ByteBuffer.wrap(bytes)
                return WateringScript(buffer.int, buffer.short, buffer.int,
                        buffer.int, buffer.get(), buffer.get() == 1.toByte())
            }
        }

        fun toBytes(): ByteArray {
            val buffer = ByteBuffer.allocate(BYTES_SIZE)
            buffer.putInt(intervalSeconds)
            buffer.putShort(durationSeconds)
            buffer.putInt(lastWateringTime)
            buffer.putInt(nextWateringTime)
            buffer.put(purposeId)
            buffer.put(enabled.toByte())
            return buffer.array()
        }
    }


    companion object {
        fun Long.toBytes(): ByteArray {
            val buffer = ByteBuffer.allocate(java.lang.Long.SIZE / 8)
            buffer.putLong(this)
            return buffer.array()
        }

        fun Boolean.toByte(): Byte {
            return if (this) 1 else 0
        }
    }

}