package top.fifthlight.touchcontroller.layout

import top.fifthlight.touchcontroller.asset.Textures
import top.fifthlight.touchcontroller.control.JumpButton
import top.fifthlight.touchcontroller.proxy.data.IntSize

private fun Context.JumpButtonTexture(size: IntSize, clicked: Boolean, classic: Boolean) {
    withAlign(align = Align.CENTER_CENTER, size = size) {
        if (classic) {
            if (state == HudState.NORMAL) {
                Texture(id = Textures.JUMP_CLASSIC)
            } else {
                Texture(id = Textures.JUMP_FLYING)
            }
        } else {
            if (clicked) {
                Texture(id = Textures.JUMP_ACTIVE)
            } else {
                Texture(id = Textures.JUMP)
            }
        }
    }
}

fun Context.RawJumpButton(classic: Boolean = true, size: IntSize = this.size, swipe: Boolean = false) {
    if (classic || state == HudState.NORMAL) {
        val (newPointer, clicked) = if (swipe) {
            SwipeButton(id = "jump") { clicked ->
                JumpButtonTexture(size, clicked, classic)
            }
        } else {
            Button(id = "jump") { clicked ->
                JumpButtonTexture(size, clicked, classic)
            }
        }
        if (classic && state == HudState.FLYING) {
            if (newPointer && status.cancelFlying.click(timer.tick)) {
                result.cancelFlying = true
            }
        } else {
            result.jump = result.jump || clicked
        }
    }
}

fun Context.JumpButton(config: JumpButton) {
    RawJumpButton(classic = config.classic)
}