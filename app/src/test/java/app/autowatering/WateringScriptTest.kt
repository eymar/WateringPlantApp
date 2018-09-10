package app.autowatering

import app.autowatering.core.RemoteClientImpl
import app.autowatering.core.WateringClient
import app.autowatering.core.WateringClientImpl
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class WateringScriptTest {

    @Test
    fun `gn cmd set time, responds success`() = runBlocking {
        val response = ByteArray(64)
        response[0] = WateringClientImpl.CMD_SET_TIME
        response[1] = 1

        val inputStream = ByteArrayInputStream(response)
        val outputStream = ByteArrayOutputStream()

        val remoteClient = RemoteClientImpl(RemoteClientImpl.Configuration(inputStream,
                outputStream, 1000, 1000, 64, 20))

        launch {
            remoteClient.start()
        }

        val wateringClient = WateringClientImpl(remoteClient)
        val result = wateringClient.setTime(10000)

        assertTrue("Set time result is success", result)
    }

    @Test
    fun `gn cmd get time, responds success`() = runBlocking {
        val response = ByteBuffer.wrap(ByteArray(64))
        response.put(WateringClientImpl.CMD_GET_TIME)
        response.put(1)
        response.putInt(1234567)


        val inputStream = ByteArrayInputStream(response.array())
        val outputStream = ByteArrayOutputStream()

        val remoteClient = RemoteClientImpl(RemoteClientImpl.Configuration(inputStream,
                outputStream, 1000, 1000, 64, 20))

        launch {
            remoteClient.start()
        }

        val wateringClient = WateringClientImpl(remoteClient)
        val result = wateringClient.getTimeSeconds()

        assertTrue("Get time result is success", result == 1234567)
    }

    @Test
    fun `gn cmd get scripts, responds success`() = runBlocking {
        val response = ByteBuffer.allocate(64)

        val script1 = WateringClient.WateringScript(100, 200,
                1, 2, 0, false)

        val script2 = WateringClient.WateringScript(500, 600,
                10, 20, 10, true)

        response.put(WateringClientImpl.CMD_GET_SCRIPTS)
        response.put(1)
        response.put(2) // the count of scripts
        response.put(script1.toBytes())
        response.put(script2.toBytes())

        val inputStream = ByteArrayInputStream(response.array())
        val outputStream = ByteArrayOutputStream()

        val remoteClient = RemoteClientImpl(RemoteClientImpl.Configuration(inputStream,
                outputStream, 1000, 1000, 64, 20))

        launch {
            remoteClient.start()
        }

        val wateringClient = WateringClientImpl(remoteClient)
        val result = wateringClient.getAllUsualScripts()

        assertTrue("Returns 2 scripts", result.size == 2)
        assertTrue("Response Script[0] equals to expected", result[0] == script1)
        assertTrue("Response Script[1] equals to expected", result[1] == script2)
    }

    @Test
    fun `composes correct bytes from instance`() {
        val s = WateringClient.WateringScript(10, 2,
                4, 16, 1, true)

        val bytes = ByteBuffer.wrap(s.toBytes())

        val interval = bytes.int
        val duration = bytes.short
        val lastTime = bytes.int
        val nextTime = bytes.int
        val purpose = bytes.get()
        val enabled = bytes.get() == 1.toByte()


        assertTrue(interval == 10)
        assertTrue(duration == 2.toShort())
        assertTrue(lastTime == 4)
        assertTrue(nextTime == 16)
        assertTrue(purpose == 1.toByte())
        assertTrue(enabled)
    }

    @Test
    fun `back and forth convertion`() {
        val source = WateringClient.WateringScript(1280, 432,
                4423, 3453416, 0, false)

        val result = WateringClient.WateringScript.fromBytes(source.toBytes())

        assertTrue(source == result)

    }
}