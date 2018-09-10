package app.autowatering.core


interface RemoteClient {

    suspend fun sendCommand(bytes: ByteArray): ByteArray

}