package app.autowatering.core

import java.nio.ByteBuffer
import app.autowatering.core.WateringClient.WateringScript

class WateringClientImpl(private val remoteClient: RemoteClient) : WateringClient {

    companion object {
        private const val SIZE_OF_CMD = 1

        const val CMD_SET_TIME: Byte = 0x05
        const val CMD_GET_TIME: Byte = 0x06
        const val CMD_COMMIT_SCRIPT: Byte = 0x07
        const val CMD_GET_SCRIPTS: Byte = 0x08
        const val CMD_DO_ONE_SHOT_SCRIPT: Byte = 0x09
    }

    override suspend fun setTime(timestampSeconds: Int): Boolean {
        val cmd = CMD_SET_TIME
        val request = ByteBuffer.allocate(SIZE_OF_CMD + Integer.SIZE / 8)
        request.put(cmd).putInt(timestampSeconds)

        val response = Response(remoteClient.sendCommand(request.array()))
        response.assertCmd(cmd)

        return response.success
    }

    override suspend fun getTimeSeconds(): Int {
        val cmd = CMD_GET_TIME
        val request = ByteBuffer.allocate(SIZE_OF_CMD)
        request.put(CMD_GET_TIME)

        val response = Response(remoteClient.sendCommand(request.array()))
        response.assertCmd(cmd)
        response.assertSuccess()
        response.assertHasData(4) // the size of int

        return ByteBuffer.wrap(response.data).int
    }

    override suspend fun getWaterLevelStatus(): Boolean {
        // TODO: send a command and return actual result
        return true
    }

    override suspend fun launchOneShotScript(script: WateringClient.WateringScript): Boolean {
        val cmd = CMD_DO_ONE_SHOT_SCRIPT
        val request = ByteBuffer.allocate(SIZE_OF_CMD + WateringClient.WateringScript.BYTES_SIZE)
        request.put(cmd).put(script.toBytes())

        val response = Response(remoteClient.sendCommand(request.array()))
        response.assertCmd(cmd)

        return response.success
    }

    override suspend fun commitUsualScript(script: WateringClient.WateringScript): Boolean {
        val cmd = CMD_COMMIT_SCRIPT
        val request = ByteBuffer.allocate(SIZE_OF_CMD + WateringClient.WateringScript.BYTES_SIZE)
        request.put(cmd).put(script.toBytes())

        val response = Response(remoteClient.sendCommand(request.array()))
        response.assertCmd(cmd)

        return response.success
    }

    override suspend fun getAllUsualScripts(): Array<WateringScript> {
        val cmd = CMD_GET_SCRIPTS

        val request = ByteBuffer.allocate(SIZE_OF_CMD)
        request.put(cmd)

        val response = Response(remoteClient.sendCommand(request.array()))
        response.assertCmd(cmd)
        response.assertSuccess()

        // min expected size = 1, at least count of scripts should be there
        response.assertHasData(1)

        return with(ByteBuffer.wrap(response.data)) {
            val scriptsCount = get().toInt()
            response.assertHasData(1 + scriptsCount * WateringScript.BYTES_SIZE)

            val tmp = ByteArray(WateringScript.BYTES_SIZE)
            return@with Array(scriptsCount) {
                get(tmp)
                WateringScript.fromBytes(tmp)
            }
        }
    }


    class Response(val bytes: ByteArray) {
        val cmd: Byte = bytes[0]
        val success: Boolean = bytes[1] == 1.toByte()
        val data: ByteArray = bytes.sliceArray(2 until bytes.size)

        init {
            println("CMD: ${bytes[0]}; \nResponse: ${bytes.joinToString(", ")} ")
        }

        fun hasData() = data.isNotEmpty()

        fun assertCmd(cmd: Byte) {
            if (this.cmd != cmd)
                throw UnexpectedResponse("CMD: $cmd but response ${this.cmd}")
        }

        fun assertHasData(expectedSize: Int) {
            if (data.size < expectedSize) {
                throw UnexpectedResponse("CMD: $cmd, expected size: $expectedSize, " +
                        "but response size: ${data.size}")
            }
        }

        fun assertSuccess() {
            if (!success)
                throw UnexpectedResponse("CMD: $cmd - Failed")
        }
    }

    class UnexpectedResponse(message: String) : IllegalStateException(message)
}