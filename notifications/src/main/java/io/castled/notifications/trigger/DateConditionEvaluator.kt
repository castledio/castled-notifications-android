package io.castled.notifications.trigger

import io.castled.notifications.trigger.enums.OperationType
import io.castled.notifications.trigger.models.BetweenOperation
import io.castled.notifications.trigger.models.PropertyOperation
import io.castled.notifications.trigger.models.SingleValueOperation

internal class DateConditionEvaluator : ParamsConditionEvaluator() {

    // TODO: Need to fix the date evaluation
    override fun evaluateCondition(value: Any?, propertyOperation: PropertyOperation): Boolean {
        val dateText = value as String?
        return when (propertyOperation.type) {
            OperationType.BETWEEN -> {
                val dateInterval = propertyOperation as BetweenOperation
                dateText != null && dateInterval.from < dateText && dateInterval.to > dateText
            }
            OperationType.EQ -> {
                val dateEquals = propertyOperation as SingleValueOperation
                dateEquals.value == dateText
            }
            OperationType.NEQ -> {
                if (dateText == null) {
                    return true
                }
                val dateNotEquals = propertyOperation as SingleValueOperation
                dateNotEquals.value != dateText
            }
            else -> {
                logger.error("Operations type ${propertyOperation.propertyType} not supported for date operand")
                return false
            }
        }
    }
}