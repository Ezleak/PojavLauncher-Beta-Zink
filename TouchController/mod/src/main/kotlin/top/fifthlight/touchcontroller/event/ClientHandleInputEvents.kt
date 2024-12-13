package top.fifthlight.touchcontroller.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.MinecraftClient
import top.fifthlight.touchcontroller.event.ClientHandleInputEvents.HandleInput

object ClientHandleInputEvents {
    val HANDLE_INPUT: Event<HandleInput> = EventFactory.createArrayBacked(
        HandleInput::class.java
    ) { listeners: Array<HandleInput> ->
        HandleInput { client, context ->
            for (event in listeners) {
                event.onHandleInput(client, context)
            }
        }
    }

    interface InputContext {
        fun doAttack(): Boolean
        fun doItemPick()
        fun doItemUse()
        fun hitEmptyBlockState()
    }

    fun interface HandleInput {
        fun onHandleInput(client: MinecraftClient, context: InputContext)
    }
}
