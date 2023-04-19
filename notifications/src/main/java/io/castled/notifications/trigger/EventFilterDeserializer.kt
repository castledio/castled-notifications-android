package io.castled.notifications.trigger

import com.google.gson.JsonParseException
import io.castled.notifications.trigger.enums.FilterType
import io.castled.notifications.trigger.enums.JoinType
import io.castled.notifications.trigger.enums.OperationType
import io.castled.notifications.trigger.enums.PropertyType
import io.castled.notifications.trigger.models.*
import io.castled.notifications.trigger.models.BetweenOperation
import io.castled.notifications.trigger.models.PropertyFilter
import io.castled.notifications.trigger.models.PropertyOperation
import io.castled.notifications.trigger.models.SingleValueOperation
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.*

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = EventFilter::class)
internal object EventFilterDeserializer : KSerializer<EventFilter> {

    override fun deserialize(decoder: Decoder): EventFilter {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("This serializer can only be used with JSON format")
        val json = jsonDecoder.decodeJsonElement()
        return deserializeEventFilter(json)
    }

    private fun deserializeEventFilter(json: JsonElement): EventFilter {
        val filterType = (json.jsonObject["type"] as JsonPrimitive?)
            ?.let { FilterType.valueOf(it.content) }
        return when (filterType) {
            FilterType.GROUP -> {
                deserializeGroupFilter(json)
            }
            FilterType.HAVING_PROPERTY -> {
                deserializePropertyFilter(json)
            }
            else -> {
                throw JsonParseException("Unrecognized filter type!")
            }
        }
    }

    private fun deserializeGroupFilter(json: JsonElement): GroupFilter {
        val jsonObject = json.jsonObject
        val joinType =
            (jsonObject["joinType"] as JsonPrimitive?)?.let { JoinType.valueOf(it.content) }
                ?: throw JsonParseException("Joint type null!")
        val filters = jsonObject["filters"]?.jsonArray
        val eventFilters = mutableListOf<EventFilter>()
        filters?.let {
            for (element in it) {
                eventFilters.add(deserializeEventFilter(element))
            }
        }
        return GroupFilter(joinType, eventFilters)
    }

    private fun deserializePropertyFilter(json: JsonElement): PropertyFilter {
        val jsonObject = json.jsonObject
        val name = (jsonObject["name"] as JsonPrimitive).content
        val operationElement = jsonObject["operation"]
            ?: throw JsonParseException("operation field is null!")
        val propertyOperation = deserializeOperation(operationElement)
        return PropertyFilter(name, propertyOperation)
    }

    private fun deserializeOperation(json: JsonElement): PropertyOperation {
        val jsonObject = json.jsonObject
        val operationType = (jsonObject["type"] as JsonPrimitive?)
            ?.let { OperationType.valueOf(it.content) }
            ?: throw JsonParseException("operation type null!")
        val propertyType = (jsonObject["propertyType"] as JsonPrimitive?)
            ?.let { PropertyType.valueOf(it.content) }
            ?: throw JsonParseException("property type null!")
        return when (operationType) {
            OperationType.EQ, OperationType.GT, OperationType.LT, OperationType.GTE, OperationType.LTE, OperationType.NEQ -> {
                val value = (jsonObject["value"] as JsonPrimitive?)?.content
                    ?: throw JsonParseException("value null!")
                SingleValueOperation(value, operationType, propertyType)
            }
            OperationType.BETWEEN -> {
                val from = (jsonObject["from"] as JsonPrimitive?)?.content
                    ?: throw JsonParseException("from value null!")
                val to = (jsonObject["to"] as JsonPrimitive?)?.content
                    ?: throw JsonParseException("to value null!")
                BetweenOperation(from, to, operationType, propertyType)
            }
            else -> throw JsonParseException("Unrecognized operation type!")
        }
    }
}