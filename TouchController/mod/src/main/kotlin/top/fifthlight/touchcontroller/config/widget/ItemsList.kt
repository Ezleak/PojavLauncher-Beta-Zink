package top.fifthlight.touchcontroller.config.widget

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import top.fifthlight.touchcontroller.config.ObservableValue

class ItemsList(
    client: MinecraftClient,
    width: Int = 0,
    height: Int = 0,
    y: Int = 0,
    itemHeight: Int,
    items: PersistentList<Item> = persistentListOf()
) : AlwaysSelectedEntryListWidget<ItemsList.Entry>(
    client, width, height, y, itemHeight
) {
    companion object {
        private val SLOT_TEXTURE: Identifier = Identifier.ofVanilla("container/slot")
    }

    var items = items
        set(value) {
            field = value
            clearEntries()
            for (item in value) {
                addEntry(Entry(client, item))
            }
        }

    init {
        for (item in items) {
            addEntry(Entry(client, item))
        }
    }

    val selectedIndex = ObservableValue<Int?>(null)

    override fun setFocused(focused: Element?) {
        super.setFocused(focused)
        selectedIndex.value = focused?.let { element -> children().indexOf(element).takeIf { it != -1 } }
    }

    class Entry(
        private val client: MinecraftClient,
        item: Item
    ) : AlwaysSelectedEntryListWidget.Entry<Entry>() {
        private val itemStack = ItemStack(item)

        override fun render(
            context: DrawContext,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            renderIcon(context, x + 1, y + 1, itemStack)
            context.drawText(client.textRenderer, itemStack.name, x + 24, y + 6, Colors.WHITE, false)
        }

        override fun getNarration(): Text = itemStack.name

        private fun renderIcon(context: DrawContext, x: Int, y: Int, iconItem: ItemStack) {
            context.drawGuiTexture(
                RenderLayer::getGuiTextured,
                SLOT_TEXTURE,
                x,
                y,
                18,
                18
            )
            if (!iconItem.isEmpty) {
                context.drawItemWithoutEntity(iconItem, x + 1, y + 1)
            }
        }
    }
}