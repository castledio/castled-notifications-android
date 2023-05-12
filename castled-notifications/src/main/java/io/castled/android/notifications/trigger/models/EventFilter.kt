package io.castled.android.notifications.trigger.models

import io.castled.android.notifications.trigger.enums.FilterType
import io.castled.android.notifications.trigger.enums.JoinType
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = EventFilterDeserializer::class)
sealed class EventFilter {
    abstract val type: FilterType
}

@Serializable
class GroupFilter(val joinType: JoinType, val filters: List<EventFilter>?) : EventFilter() {
    override val type = FilterType.GROUP
}

@Serializable
internal class PropertyFilter(
    val name: String,
    @Serializable(with = PropertyOperationDeserializer::class) val operation: PropertyOperation
) : EventFilter() {
    override val type = FilterType.HAVING_PROPERTY
}

internal object EventFilterDeserializer :
    JsonContentPolymorphicSerializer<EventFilter>(EventFilter::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<EventFilter> {
        val filterType = element.jsonObject["type"]?.jsonPrimitive
            ?.let { FilterType.valueOf(it.content) }
            ?: throw SerializationException("propertyType field not found. Serialization failed.")
        return when (filterType) {
            FilterType.GROUP -> GroupFilter.serializer()
            FilterType.HAVING_PROPERTY -> PropertyFilter.serializer()
        }
    }
}

