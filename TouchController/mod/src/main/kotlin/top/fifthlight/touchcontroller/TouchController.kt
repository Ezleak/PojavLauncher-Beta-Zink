package top.fifthlight.touchcontroller

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import top.fifthlight.touchcontroller.config.TouchControllerConfigHolder
import top.fifthlight.touchcontroller.di.appModule
import top.fifthlight.touchcontroller.event.ClientHandleInputEvents
import top.fifthlight.touchcontroller.event.ClientRenderEvents
import top.fifthlight.touchcontroller.event.KeyboardInputEvents
import top.fifthlight.touchcontroller.proxy.server.LauncherSocketProxyServer
import top.fifthlight.touchcontroller.proxy.server.localhostLauncherSocketProxyServer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback as FabricHudRenderCallback
import top.fifthlight.touchcontroller.event.HudRenderCallback as TouchControllerHudRenderCallback

private val logger = LoggerFactory.getLogger(TouchController::class.java)

data class SocketProxyHolder(
    var socketProxy: LauncherSocketProxyServer? = null
)

object TouchController : ClientModInitializer {
    const val NAMESPACE = "touchcontroller"

    override fun onInitializeClient() {
        logger.info("Loading TouchController…")

        val app = startKoin {
            slf4jLogger()
            modules(appModule)
        }

        val socketProxyHolder: SocketProxyHolder = app.koin.get()
        val socketPort = System.getenv("TOUCH_CONTROLLER_PROXY")?.toIntOrNull()
        socketPort?.let { localhostLauncherSocketProxyServer(it) }?.apply {
            socketProxyHolder.socketProxy = this
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                start()
            }
        }

        app.koin.initialize()
    }

    private fun Koin.initialize() {
        top.fifthlight.touchcontroller.logger.info("Client proxy set, initialize mod")
        val configHolder: TouchControllerConfigHolder = get()
        configHolder.load()
        FabricHudRenderCallback.EVENT.register(get())
        TouchControllerHudRenderCallback.CROSSHAIR.register(get())
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(get())
        WorldRenderEvents.START.register(get())
        KeyboardInputEvents.END_INPUT_TICK.register(get())
        ClientRenderEvents.START_TICK.register(get())
        ClientHandleInputEvents.HANDLE_INPUT.register(get())
        ClientPlayConnectionEvents.JOIN.register(get())

    }
}
