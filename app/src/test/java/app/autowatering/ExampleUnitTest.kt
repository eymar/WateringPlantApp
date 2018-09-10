package app.autowatering

import app.autowatering.core.RemoteClientImpl
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.test.TestCoroutineContext
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeoutException

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val context = TestCoroutineContext()

        runBlocking {
            launch {
                throw NullPointerException()
            }.join()
        }

        //if (context.exceptions.isNotEmpty()) {
          //  throw context.exceptions.first()
        //}
    }

    @Test
    fun `given output stream write blocks, throws WriteFailed`() = runBlocking {
        val inpStream = ByteArrayInputStream(byteArrayOf())

        val outStream = object : ByteArrayOutputStream() {
            override fun write(b: ByteArray?) {
                while (true) {
                }
            }
        }

        val remoteClient = RemoteClientImpl(RemoteClientImpl.Configuration(
                inpStream, outStream, 1000, 1000, 64, 20
        ))

        val context = TestCoroutineContext()

        val job = launch(context) {
            remoteClient.sendCommand(byteArrayOf(1, 2, 3))
        }

        context.advanceTimeBy(10000)
        job.join()

        assertTrue(context.exceptions.first() is RemoteClientImpl.WriteFailed)
        assertTrue(context.exceptions.first().cause is TimeoutException)
    }

    @Test
    fun `given output stream write throws, re-throws it`() = runBlocking {
        val inpStream = ByteArrayInputStream(byteArrayOf())

        val outStream = object : ByteArrayOutputStream() {
            override fun write(b: ByteArray?) {
                throw IOException()
            }
        }

        val remoteClient = RemoteClientImpl(RemoteClientImpl.Configuration(
                inpStream, outStream, 1000, 1000, 64, 20
        ))

        val context = TestCoroutineContext()

        val job = launch(context) {
            remoteClient.sendCommand(byteArrayOf(1, 2, 3))
        }

        context.triggerActions()
        job.join()

        assertTrue(context.exceptions.first() is IOException)
    }

    @Test
    fun `given input stream exceeds buffer, throws exception`() = runBlocking {
        val inpStream = ByteArrayInputStream(ByteArray(2000))

        val outStream = ByteArrayOutputStream()

        val remoteClient = RemoteClientImpl(RemoteClientImpl.Configuration(
                inpStream, outStream, 1000, 1000, 64, 20
        ))

        val context = TestCoroutineContext()

        val job = launch(context) {
            val res = remoteClient.sendCommand(byteArrayOf(1, 2, 3))
            println(res.size)
        }

        context.triggerActions()
        job.join()

        assertTrue(context.exceptions.first() is IllegalStateException)
    }
}
