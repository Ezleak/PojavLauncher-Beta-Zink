package top.fifthlight.touchcontroller.config

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import top.fifthlight.touchcontroller.TouchController
import top.fifthlight.touchcontroller.ext.TouchControllerLayoutSerializer
import java.nio.file.FileAlreadyExistsException
import kotlin.io.path.createDirectory
import kotlin.io.path.readText
import kotlin.io.path.writeText

class TouchControllerConfigHolder : KoinComponent {
    private val fabricLoader: FabricLoader = get()
    private val logger = LoggerFactory.getLogger(TouchControllerConfig::class.java)
    private val configDir = fabricLoader.configDir.resolve(TouchController.NAMESPACE)
    private val configFile = configDir.resolve("config.json")
    private val layoutFile = configDir.resolve("layout.json")

    private val json: Json by inject()
    private var configLoaded = atomic(false)
    private val _config = MutableStateFlow(TouchControllerConfig())
    val config = _config.asStateFlow()
    private val _layout = MutableStateFlow(defaultTouchControllerLayout)
    val layout = _layout.asStateFlow()

    fun load() {
        if (configLoaded.compareAndSet(expect = false, update = true)) {
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        _config.value = json.decodeFromString(configFile.readText())
                        _layout.value = json.decodeFromString(TouchControllerLayoutSerializer(), layoutFile.readText())
                    } catch (ex: Exception) {
                        logger.warn("Failed to read config: ", ex)
                    }
                }
            }
        }
    }

    fun saveConfig(config: TouchControllerConfig) {
        _config.value = config
    }

    fun saveLayout(layout: TouchControllerLayout) {
        _layout.value = layout
    }

    init {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            fun createConfigDirectory() {
                try {
                    configDir.createDirectory()
                } catch (_: FileAlreadyExistsException) {
                }
            }
            launch {
                config.collectLatest { config ->
                    if (!configLoaded.value) {
                        return@collectLatest
                    }
                    withContext(Dispatchers.IO) {
                        try {
                            createConfigDirectory()
                            configFile.writeText(json.encodeToString(config))
                        } catch (ex: Exception) {
                            logger.warn("Failed to write config: ", ex)
                        }
                    }
                }
            }
            launch {
                val serializer = TouchControllerLayoutSerializer()
                layout.collectLatest { layout ->
                    if (!configLoaded.value) {
                        return@collectLatest
                    }
                    withContext(Dispatchers.IO) {
                        try {
                            createConfigDirectory()
                            layoutFile.writeText(
                                json.encodeToString(serializer, layout)
                            )
                        } catch (ex: Exception) {
                            logger.warn("Failed to write layout: ", ex)
                        }
                    }
                }
            }
        }
    }
}