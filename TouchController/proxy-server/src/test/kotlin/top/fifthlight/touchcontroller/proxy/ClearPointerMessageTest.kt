package top.fifthlight.touchcontroller.proxy

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Test
import top.fifthlight.touchcontroller.proxy.message.ClearPointerMessage
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClearPointerMessageTest: MessageTest() {
    @Test
    fun test() {
        val (server, client) = createProxyPair()
        try {
            runBlocking {
                launch { server.start() }
                launch { client.start() }

                server.listening.first { it }

                client.send(ClearPointerMessage)

                var message: ClearPointerMessage? = null
                while (message == null) {
                    server.receive { msg ->
                        println("OK")
                        assertTrue(msg is ClearPointerMessage)
                        assertNull(message)
                        message = msg
                    }
                    yield()
                }
                println("CLOSED")

                cancel()
                server.close()
                client.close()
            }
        } catch (_: CancellationException) {}
    }
}