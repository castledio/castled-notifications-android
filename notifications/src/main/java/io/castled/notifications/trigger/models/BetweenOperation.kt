package io.castled.notifications.trigger.models

import io.castled.notifications.trigger.enums.OperationType
import io.castled.notifications.trigger.enums.PropertyType

internal class BetweenOperation(
    val from: String,
    val to: String,
    operationType: OperationType,
    propertyType: PropertyType
) : PropertyOperation(
    operationType, propertyType
)