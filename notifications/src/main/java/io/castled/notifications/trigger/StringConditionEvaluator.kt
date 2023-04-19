package io.castled.notifications.trigger

import io.castled.notifications.trigger.enums.OperationType
import io.castled.notifications.trigger.models.PropertyOperation
import io.castled.notifications.trigger.models.SingleValueOperation

internal class StringConditionEvaluator : ParamsConditionEvaluator() {

    override fun evaluateCondition(value: Any?, propertyOperation: PropertyOperation): Boolean {
        val textValue = value as String? ?: return false
        return when (propertyOperation.operationType) {
            OperationType.EQ -> (propertyOperation as SingleValueOperation).value == textValue
            OperationType.NEQ -> (propertyOperation as SingleValueOperation).value != textValue
            else -> {
                logger.error("Operations type ${propertyOperation.propertyType} not supported for string operand")
                return false
            }
        }
    }
}