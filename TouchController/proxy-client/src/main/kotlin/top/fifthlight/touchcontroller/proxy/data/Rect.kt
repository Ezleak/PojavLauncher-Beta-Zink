package top.fifthlight.touchcontroller.proxy.data

data class Rect(
    val offset: Offset = Offset.ZERO,
    val size: Size
) {
    companion object {
        val ONE = Rect(
            offset = Offset.ZERO,
            size = Size.ONE
        )
    }

    val left
        get() = offset.x
    val top
        get() = offset.y
    val right
        get() = offset.x + size.width
    val bottom
        get() = offset.y + size.height
}