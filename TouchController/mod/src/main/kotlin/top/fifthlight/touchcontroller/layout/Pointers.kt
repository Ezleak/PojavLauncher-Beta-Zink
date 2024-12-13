package top.fifthlight.touchcontroller.layout

import net.minecraft.util.Colors
import top.fifthlight.touchcontroller.ext.withTranslate

fun Context.Pointers() {
    drawQueue.enqueue { drawContext, textRenderer ->
        pointers.forEach { (id, pointer) ->
            drawContext.withTranslate(pointer.scaledOffset) {
                drawContext.fill(-1, -1, 1, 1, Colors.WHITE)
                drawContext.drawBorder(-4, -4, 8, 8, Colors.WHITE)
                val text = "$id"
                drawContext.drawText(textRenderer, text, -textRenderer.getWidth(text) / 2, 8, Colors.WHITE, false)
            }
        }
    }
}