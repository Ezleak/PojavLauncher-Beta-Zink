package top.fifthlight.touchcontroller.proxy.data

data class IntRect(
    val offset: IntOffset,
    val size: IntSize
) {
    val left
        get() = offset.x
    val top
        get() = offset.y
    val right
        get() = offset.x + size.width
    val bottom
        get() = offset.y + size.height
}