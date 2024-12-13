package top.fifthlight.touchcontroller.ext

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import kotlin.jvm.optionals.getOrNull

@JvmInline
value class ItemsList(val items: PersistentList<Item> = persistentListOf())

class ItemsListSerializer : KSerializer<ItemsList> {
    private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<PersistentList<Item>>()

    private val itemSerializer = serializer<String>()

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: ItemsList) {
        val ids = value.items.mapNotNull {
            Registries.ITEM.getKey(it).getOrNull()?.value?.toString()
        }
        ListSerializer(itemSerializer).serialize(encoder, ids)
    }

    override fun deserialize(decoder: Decoder): ItemsList {
        return ItemsList(ListSerializer(itemSerializer).deserialize(decoder).mapNotNull {
            Registries.ITEM.getOptionalValue(Identifier.of(it)).getOrNull()
        }.toPersistentList())
    }
}