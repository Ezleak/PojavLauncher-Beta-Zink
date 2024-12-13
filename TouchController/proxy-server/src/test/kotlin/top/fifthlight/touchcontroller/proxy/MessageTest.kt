package top.fifthlight.touchcontroller.proxy

import top.fifthlight.touchcontroller.proxy.client.LauncherSocketProxyClient
import top.fifthlight.touchcontroller.proxy.client.localhostLauncherSocketProxyClient
import top.fifthlight.touchcontroller.proxy.server.LauncherSocketProxyServer
import top.fifthlight.touchcontroller.proxy.server.localhostLauncherSocketProxyServer
import kotlin.random.Random

abstract class MessageTest {
    fun createProxyPair(): Pair<LauncherSocketProxyServer, LauncherSocketProxyClient> {
        val port = Random.nextInt(32768, 65535)
        println("Port: $port")
        val serverProxy = localhostLauncherSocketProxyServer(port) ?: error("Failed to create server proxy")
        val clientProxy = localhostLauncherSocketProxyClient(port) ?: error("Failed to create client proxy")
        return Pair(serverProxy, clientProxy)
    }
}