package top.fifthlight.touchcontroller.proxy.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage
import java.io.IOException
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class LauncherSocketProxyClient internal constructor(
    private val channel: DatagramChannel,
    private val address: SocketAddress
): AutoCloseable {
    private val sendQueue = MutableSharedFlow<ProxyMessage>(0, 256)

    suspend fun start() {
        withContext(Dispatchers.IO) {
            val buffer = ByteBuffer.allocate(1024)
            sendQueue.collect { message ->
                message.encode(buffer)
                buffer.flip()
                runInterruptible(Dispatchers.IO) { channel.send(buffer, address) }
                println("SEND")
                buffer.clear()
            }
        }
    }

    fun trySend(message: ProxyMessage) {
        sendQueue.tryEmit(message)
    }

    suspend fun send(message: ProxyMessage) {
        sendQueue.emit(message)
    }

    override fun close() {
        channel.close()
    }
}

fun localhostLauncherSocketProxyClient(port: Int): LauncherSocketProxyClient? {
    val address = InetSocketAddress(
        Inet6Address.getByAddress(
            "localhost",
            byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        ), port
    )
    return try {
        LauncherSocketProxyClient(DatagramChannel.open(), address)
    } catch (ex: IOException) {
        null
    }
}

fun runProxy(proxy: LauncherSocketProxyClient) {
    runBlocking {
        proxy.start()
    }
}