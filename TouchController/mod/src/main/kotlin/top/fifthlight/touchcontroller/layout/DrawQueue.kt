package top.fifthlight.touchcontroller.layout

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext


typealias DrawCall = (DrawContext, TextRenderer) -> Unit

class DrawQueue {
    private val items = mutableListOf<DrawCall>()

    fun enqueue(block: DrawCall) {
        items.add(block)
    }

    fun execute(drawContext: DrawContext, textRenderer: TextRenderer) {
        items.forEach { it.invoke(drawContext, textRenderer) }
    }
}