package top.fifthlight.touchcontroller.model

import net.minecraft.client.MinecraftClient
import top.fifthlight.touchcontroller.ext.scaledSize
import top.fifthlight.touchcontroller.state.GlobalState

class GlobalStateModel {
    var state = GlobalState()
        private set

    fun update(client: MinecraftClient) {
        state = state.copy(
            inGame = client.currentScreen == null,
            windowSize = client.window.scaledSize
        )
    }
}
