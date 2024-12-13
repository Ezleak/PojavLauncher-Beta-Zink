package top.fifthlight.touchcontroller.handler

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import top.fifthlight.touchcontroller.model.ControllerHudModel

class HudCallbackHandler : HudRenderCallback, KoinComponent {
    private val client: MinecraftClient by inject()
    private val controllerHudModel: ControllerHudModel by inject()

    override fun onHudRender(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val queue = controllerHudModel.pendingDrawQueue
        queue?.let {
            queue.execute(drawContext, client.textRenderer)
            controllerHudModel.pendingDrawQueue = null
        }
    }
}
