package top.fifthlight.touchcontroller.config.widget

import net.minecraft.client.gui.widget.Positioner
import net.minecraft.client.gui.widget.Widget
import net.minecraft.client.gui.widget.WrapperWidget
import top.fifthlight.touchcontroller.config.widget.BorderLayout.Direction.HORIZONTAL
import top.fifthlight.touchcontroller.config.widget.BorderLayout.Direction.VERTICAL
import java.util.function.Consumer

class BorderLayout(
    left: Int = 0,
    top: Int = 0,
    width: Int = 0,
    height: Int = 0,
    var direction: Direction = HORIZONTAL
) : WrapperWidget(left, top, width, height) {
    enum class Direction {
        HORIZONTAL,
        VERTICAL
    }

    private class Element(
        widget: Widget,
        positioner: Positioner,
        private val onSizeChanged: (widget: Widget, width: Int, height: Int) -> Unit = { _, _, _ -> }
    ) : WrappedElement(widget, positioner) {
        fun setSize(width: Int, height: Int) = onSizeChanged(
            widget,
            width - positioner.marginLeft - positioner.marginRight,
            height - positioner.marginTop - positioner.marginBottom
        )
    }

    private var firstElement: Element? = null
    private var secondElement: Element? = null
    private var centerElement: Element? = null

    override fun forEachElement(consumer: Consumer<Widget>) {
        firstElement?.let { consumer.accept(it.widget) }
        centerElement?.let { consumer.accept(it.widget) }
        secondElement?.let { consumer.accept(it.widget) }
    }

    fun setDimension(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    fun setFirstElement(
        widget: Widget,
        positioner: Positioner = Positioner.create(),
        onSizeChanged: (widget: Widget, width: Int, height: Int) -> Unit
    ) {
        firstElement = Element(widget, positioner, onSizeChanged)
    }

    fun setSecondElement(
        widget: Widget,
        positioner: Positioner = Positioner.create(),
        onSizeChanged: (widget: Widget, width: Int, height: Int) -> Unit
    ) {
        secondElement = Element(widget, positioner, onSizeChanged)
    }

    fun setCenterElement(
        widget: Widget,
        positioner: Positioner = Positioner.create(),
        onSizeChanged: (widget: Widget, width: Int, height: Int) -> Unit
    ) {
        centerElement = Element(widget, positioner, onSizeChanged)
    }

    override fun refreshPositions() {
        val first = firstElement
        val second = secondElement
        val center = centerElement

        when (direction) {
            HORIZONTAL -> {
                val leftWidth = first?.width ?: 0
                val rightWidth = second?.width ?: 0
                val centerWidth = width - leftWidth - rightWidth
                first?.let {
                    it.setX(x, x + leftWidth)
                    it.setY(y, y + height)
                    it.setSize(leftWidth, height)
                }
                center?.let {
                    it.setX(x + leftWidth, x + width - rightWidth)
                    it.setY(y, y + height)
                    it.setSize(centerWidth, height)
                }
                second?.let {
                    it.setX(x + width - rightWidth, x + width)
                    it.setY(y, y + height)
                    it.setSize(rightWidth, height)
                }
            }

            VERTICAL -> {
                val topHeight = first?.height ?: 0
                val bottomHeight = second?.height ?: 0
                val centerHeight = height - topHeight - bottomHeight
                first?.let {
                    it.setX(x, x + width)
                    it.setY(y, y + topHeight)
                    it.setSize(width, topHeight)
                }
                center?.let {
                    it.setX(x, x + width)
                    it.setY(y + topHeight, y + height - bottomHeight)
                    it.setSize(width, centerHeight)
                }
                second?.let {
                    it.setX(x, x + width)
                    it.setY(y + height - bottomHeight, y + height)
                    it.setSize(width, bottomHeight)
                }
            }
        }
        super.refreshPositions()
    }
}
