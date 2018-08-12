package app.autowatering

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import kotlinx.coroutines.experimental.withTimeoutOrNull
import kotlinx.coroutines.experimental.yield
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeoutException

class RemoteClientImpl constructor(
        private val configuration: Configuration) : RemoteClient {

    private val bytesChannel = Channel<Byte>(configuration.bufferSize)

    private val thread = newSingleThreadContext("MyOwnThread")

    private val inputStream: InputStream = configuration.inputStream
    private val outputStream: OutputStream = configuration.outputStream

    private val writeCommandBytes = ByteArray(configuration.commandSize)

    private val mutex = Mutex()
    private val mutex2 = Mutex()


    class WriteFailed(reason: Throwable) : Exception(reason)
    class ReadFailed(reason: Throwable) : Exception(reason)

    suspend fun start() {
        val async = async(thread) {
            while (true) {
                if (inputStream.available() < 1) {
                    yield()
                    continue
                }

                delay(0)

                mutex.withLock {
                    inputStream.read().takeIf { it != -1 }?.run {
                        bytesChannel.send(this.toByte())
                    }
                }
            }
        }
        async.await()
    }

    override suspend fun sendCommand(bytes: ByteArray): ByteArray {
        return mutex2.withLock {
            val buf = ByteArray(configuration.bufferSize)

            val wait = async(thread) {
                mutex.withLock {
                    System.arraycopy(bytes, 0, writeCommandBytes, 0, bytes.size)
                    outputStream.flush()
                    outputStream.write(writeCommandBytes)
                }

                buf.forEachIndexed { index, _ ->
                    buf[index] = bytesChannel.receive()
                }
            }

            withTimeoutOrNull(configuration.writeTimeoutMs) {
                wait.await()
                true
            } ?: throw WriteFailed(TimeoutException("Exceeds the send command timeout"))

            buf
        }
    }

    class Configuration(val inputStream: InputStream,
                        val outputStream: OutputStream,
                        val writeTimeoutMs: Long,
                        val readTimeoutMs: Long,
                        val bufferSize: Int,
                        val commandSize: Int)

}