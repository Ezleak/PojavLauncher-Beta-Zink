package top.fifthlight.touchcontroller.proxy

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Test
import top.fifthlight.touchcontroller.proxy.message.RemovePointerMessage
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RemovePointerMessageTest: MessageTest() {
    @Test
    fun test() {
        val (server, client) = createProxyPair()
        try {
            runBlocking {
                launch { server.start() }
                launch { client.start() }

                server.listening.first { it }

                val index = 0x12345678
                client.send(
                    RemovePointerMessage(
                        index = index,
                    )
                )

                var message: RemovePointerMessage? = null
                while (message == null) {
                    server.receive { msg ->
                        println("OK")
                        assertTrue(msg is RemovePointerMessage)
                        assertNull(message)
                        message = msg
                    }
                    yield()
                }

                val removePointerMessage = message!!
                assertEquals(index, removePointerMessage.index)

                cancel()
                server.close()
                client.close()
            }
        } catch (_: CancellationException) {}
    }
}