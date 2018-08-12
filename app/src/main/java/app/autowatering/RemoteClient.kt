package app.autowatering


interface RemoteClient {

    suspend fun sendCommand(bytes: ByteArray): ByteArray

}