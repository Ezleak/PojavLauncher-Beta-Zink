package top.fifthlight.touchcontroller.proxy.message

import java.nio.ByteBuffer

sealed class ProxyMessage {
    abstract val type: Int

    open fun encode(buffer: ByteBuffer) {
        buffer.putInt(type)
    }
}
