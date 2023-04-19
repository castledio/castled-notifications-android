package io.castled.notifications.trigger.models

import io.castled.notifications.trigger.enums.OperationType
import io.castled.notifications.trigger.enums.PropertyType
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
internal sealed class PropertyOperation(val operationType: OperationType, val propertyType: PropertyType)