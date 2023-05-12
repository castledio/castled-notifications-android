package io.castled.android.notifications.trigger

import io.castled.android.notifications.trigger.enums.OperationType
import io.castled.android.notifications.trigger.models.PropertyOperation
import io.castled.android.notifications.trigger.models.SingleValueOperation

internal class StringConditionEvaluator : ParamsConditionEvaluator() {

    override fun evaluateCondition(value: Any?, propertyOperation: PropertyOperation): Boolean {
        val textValue = value as String? ?: return false
        return when (propertyOperation.type) {
            OperationType.EQ -> (propertyOperation as SingleValueOperation).value == textValue
            OperationType.NEQ -> (propertyOperation as SingleValueOperation).value != textValue
            else -> {
                logger.error("Operations type ${propertyOperation.propertyType} not supported for string operand")
                return false
            }
        }
    }
}