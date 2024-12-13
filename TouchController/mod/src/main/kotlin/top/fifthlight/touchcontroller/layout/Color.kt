package top.fifthlight.touchcontroller.layout

fun Context.Color(color: Int) {
    drawQueue.enqueue { drawContext, _ ->
        drawContext.fill(0, 0, size.width, size.height, color)
    }
}