package top.fifthlight.touchcontroller.proxy.message

import java.nio.ByteBuffer

abstract class ProxyMessageDecoder {
    abstract fun decode(payload: ByteBuffer): ProxyMessage
}

abstract class MessageDecodeException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
class BadMessageTypeException(type: Int) : MessageDecodeException("Bad type: $type")
class BadMessageLengthException(expected: Int, actual: Int) :
    MessageDecodeException("Bad message length: expected $expected bytes, but got $actual bytes")

fun decodeMessage(type: Int, payload: ByteBuffer): ProxyMessage = when (type) {
    1 -> AddPointerMessage.DECODER
    2 -> RemovePointerMessage.DECODER
    3 -> ClearPointerMessage.DECODER
    else -> throw BadMessageTypeException(type)
}.decode(payload)
