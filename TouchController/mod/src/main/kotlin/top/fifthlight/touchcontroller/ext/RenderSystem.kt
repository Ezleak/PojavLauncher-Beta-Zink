package top.fifthlight.touchcontroller.ext

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gl.ShaderProgramKey

inline fun withBlend(crossinline block: () -> Unit) {
    RenderSystem.enableBlend()
    block()
    RenderSystem.disableBlend()
}

inline fun withBlendFunction(
    srcFactor: GlStateManager.SrcFactor,
    dstFactor: GlStateManager.DstFactor,
    srcAlpha: GlStateManager.SrcFactor,
    dstAlpha: GlStateManager.DstFactor,
    crossinline block: () -> Unit
) {
    RenderSystem.blendFuncSeparate(srcFactor, dstFactor, srcAlpha, dstAlpha)
    block()
    RenderSystem.defaultBlendFunc()
}

inline fun withShader(program: ShaderProgramKey, crossinline block: () -> Unit) {
    val originalShader = RenderSystem.getShader()
    RenderSystem.setShader(program)
    block()
    originalShader?.let {
        RenderSystem.setShader(originalShader)
    }
}