package io.castled.android.notifications.trigger.models

import io.castled.android.notifications.trigger.enums.OperationType
import io.castled.android.notifications.trigger.enums.PropertyType
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = PropertyOperationDeserializer::class)
internal sealed class PropertyOperation {
    abstract val type: OperationType
    abstract val propertyType: PropertyType
}

@Serializable
internal class SingleValueOperation(
    val value: String,
    override val type: OperationType,
    override val propertyType: PropertyType
) : PropertyOperation()

@Serializable
internal class BetweenOperation(
    val from: String,
    val to: String,
    override val type: OperationType,
    override val propertyType: PropertyType
) : PropertyOperation()

internal object PropertyOperationDeserializer :
    JsonContentPolymorphicSerializer<PropertyOperation>(PropertyOperation::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<PropertyOperation> {
        val operationType = element.jsonObject["type"]?.jsonPrimitive
            ?.let { OperationType.valueOf(it.content) }
            ?: throw SerializationException("type field not found. Serialization failed.")
        return when (operationType) {
            OperationType.EQ,
            OperationType.GT,
            OperationType.GTE,
            OperationType.LT,
            OperationType.LTE,
            OperationType.CONTAINS,
            OperationType.NEQ,
            OperationType.NOT_CONTAINS -> SingleValueOperation.serializer()
            OperationType.BETWEEN -> BetweenOperation.serializer()
        }
    }
}