package top.fifthlight.touchcontroller.layout

import net.minecraft.util.Identifier
import top.fifthlight.touchcontroller.ext.drawTexture
import top.fifthlight.touchcontroller.proxy.data.Rect

fun Context.Texture(id: Identifier, textureUv: Rect = Rect.ONE) {
    if (opacity == 1f) {
        drawQueue.enqueue { drawContext, _ ->
            drawContext.drawTexture(
                id = id,
                dstRect = Rect(size = size.toSize()),
                uvRect = textureUv
            )
        }
    } else {
        val color = ((0xFF * opacity).toInt() shl 24) or 0xFFFFFF
        drawQueue.enqueue { drawContext, _ ->
            drawContext.drawTexture(
                id = id,
                dstRect = Rect(size = size.toSize()),
                uvRect = textureUv,
                color
            )
        }
    }
}

fun Context.Texture(id: Identifier, textureUv: Rect = Rect.ONE, color: Int) {
    if (opacity == 1f) {
        drawQueue.enqueue { drawContext, _ ->
            drawContext.drawTexture(
                id = id,
                dstRect = Rect(size = size.toSize()),
                uvRect = textureUv,
                color = color
            )
        }
    } else {
        val colorWithoutAlpha = color and 0xFFFFFF
        val colorWithAlpha = ((0xFF * opacity).toInt() shl 24) or colorWithoutAlpha
        drawQueue.enqueue { drawContext, _ ->
            drawContext.drawTexture(
                id = id,
                dstRect = Rect(size = size.toSize()),
                uvRect = textureUv,
                color = colorWithAlpha
            )
        }
    }
}