package io.castled.notifications.trigger

import io.castled.notifications.trigger.enums.OperationType
import io.castled.notifications.trigger.models.PropertyOperation
import io.castled.notifications.trigger.models.SingleValueOperation

internal class BooleanConditionEvaluator : ParamsConditionEvaluator() {
    override fun evaluateCondition(value: Any?, propertyOperation: PropertyOperation): Boolean {
        val paramVal = value as Boolean?
        val conditionValue = ((propertyOperation as SingleValueOperation).value).toBoolean()

        return when (propertyOperation.type) {
            OperationType.EQ -> paramVal != null && paramVal == conditionValue
            else -> {
                logger.error("Operations type ${propertyOperation.propertyType} not supported for boolean operand")
                return false
            }
        }
    }
}