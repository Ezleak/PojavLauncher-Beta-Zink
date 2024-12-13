package top.fifthlight.touchcontroller.ext

import net.minecraft.client.gui.widget.ClickableWidget
import top.fifthlight.touchcontroller.proxy.data.IntSize

fun ClickableWidget.setDimensions(size: IntSize) = setDimensions(size.width, size.height)