package top.fifthlight.touchcontroller.config

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.DirectionalLayoutWidget
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import top.fifthlight.touchcontroller.asset.Texts
import top.fifthlight.touchcontroller.config.widget.BorderLayout
import top.fifthlight.touchcontroller.config.widget.ItemStacksGrid
import top.fifthlight.touchcontroller.config.widget.ItemsList

class ItemsListScreen(
    private val parent: Screen,
    private val name: Text,
    private val items: ObservableValue<PersistentList<Item>>
) : Screen(name) {
    private val currentClient = MinecraftClient.getInstance()

    private val itemsList = ItemsList(
        client = currentClient,
        itemHeight = 24,
        items = items.value,
    ).apply {
        this@ItemsListScreen.items.addListener {
            items = it
        }
    }

    private val content = BorderLayout().apply {
        setCenterElement(itemsList) { _, width, height ->
            itemsList.setDimensions(width, height)
        }

        ItemStacksGrid(
            client = currentClient,
            width = ItemStacksGrid.ITEM_SIZE * 12,
            message = Texts.ITEMS_SCREEN_ITEMS_LIST_MESSAGE,
            itemStacks = Registries.ITEM.map { ItemStack(it) }.toPersistentList(),
            onStackClicked = { _, stack ->
                if (stack.item !in items.value) {
                    items.value = items.value.add(stack.item)
                }
            }
        ).also { itemStacksGrid ->
            setSecondElement(itemStacksGrid) { _, width, height ->
                itemStacksGrid.setDimensions(width, height)
            }
        }
    }

    private val layout = ThreePartsLayoutWidget(this)

    private val listener: (PersistentList<Item>) -> Unit = { itemsList.items = it }

    override fun init() {
        items.addListener(listener)

        layout.apply {
            addHeader(name, client!!.textRenderer)
            addBody(content)
            DirectionalLayoutWidget.horizontal().spacing(8).apply {
                ButtonWidget.builder(Texts.ITEMS_REMOVE_TITLE) {
                    itemsList.selectedIndex.value?.let {
                        items.value = items.value.removeAt(it)
                        itemsList.selectedIndex.value = null
                    }
                }.apply {
                    tooltip(Tooltip.of(Texts.ITEMS_REMOVE_TOOLTIP))
                }.build().apply {
                    active = false
                    itemsList.selectedIndex.addListener {
                        active = it != null
                    }
                }.also { button ->
                    add(button)
                }
                ButtonWidget.builder(Texts.ITEMS_DONE_TITLE) {
                    close()
                }.apply {
                    tooltip(Tooltip.of(Texts.ITEMS_DONE_TOOLTIP))
                }.build().also { button ->
                    add(button)
                }
            }.also { footer ->
                addFooter(footer)
            }
        }
        layout.forEachChild(::addDrawableChild)
        refreshWidgetPositions()
    }

    override fun refreshWidgetPositions() {
        content.setDimension(layout.width, layout.contentHeight)
        layout.refreshPositions()
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun removed() {
        items.removeListener(listener)
    }
}