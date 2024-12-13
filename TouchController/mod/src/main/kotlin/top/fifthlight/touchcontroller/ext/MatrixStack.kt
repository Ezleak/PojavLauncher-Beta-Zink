package top.fifthlight.touchcontroller.ext

import net.minecraft.client.util.math.MatrixStack

inline fun <reified T> MatrixStack.withMatrix(crossinline block: MatrixStack.() -> T): T {
    push()
    val result = block()
    pop()
    return result
}