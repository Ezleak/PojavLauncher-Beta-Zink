package top.fifthlight.touchcontroller.proxy

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Test
import top.fifthlight.touchcontroller.proxy.data.Offset
import top.fifthlight.touchcontroller.proxy.message.AddPointerMessage
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddPointerMessageTest: MessageTest() {
    @Test
    fun test() {
        val (server, client) = createProxyPair()
        try {
            runBlocking {
                launch { server.start() }
                launch { client.start() }

                server.listening.first { it }

                val index = 0x12345678
                val position = Offset(
                    x = 0.1f,
                    y = 0.2f,
                )
                client.send(
                    AddPointerMessage(
                        index = index,
                        position = position
                    )
                )

                var message: AddPointerMessage? = null
                while (message == null) {
                    server.receive { msg ->
                        println("OK")
                        assertTrue(msg is AddPointerMessage)
                        assertNull(message)
                        message = msg
                    }
                    yield()
                }

                val addPointerMessage = message!!
                assertEquals(index, addPointerMessage.index)
                assertEquals(position, addPointerMessage.position)

                cancel()
                server.close()
                client.close()
            }
        } catch (_: CancellationException) {}
    }
}