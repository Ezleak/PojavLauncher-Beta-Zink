package top.fifthlight.touchcontroller.config.widget

import net.minecraft.client.gui.widget.SliderWidget
import net.minecraft.text.Text

class ConfigSliderWidget(
    x: Int = 0,
    y: Int = 0,
    width: Int,
    height: Int,
    value: Double,
    private val messageFormatter: (Double) -> Text,
    private val onValueChanged: (ConfigSliderWidget, Double) -> Unit
) : SliderWidget(x, y, width, height, messageFormatter(value), value) {
    override fun updateMessage() {
        message = messageFormatter(value)
    }

    fun setValue(value: Double) {
        this.value = value
    }

    override fun applyValue() {
        onValueChanged(this, value)
    }
}