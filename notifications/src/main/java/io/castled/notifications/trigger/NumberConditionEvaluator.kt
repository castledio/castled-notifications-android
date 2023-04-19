package io.castled.notifications.trigger

import io.castled.notifications.trigger.enums.OperationType
import io.castled.notifications.trigger.models.BetweenOperation
import io.castled.notifications.trigger.models.PropertyOperation
import io.castled.notifications.trigger.models.SingleValueOperation
import java.text.NumberFormat
import java.text.ParseException

internal class NumberConditionEvaluator : ParamsConditionEvaluator() {
    override fun evaluateCondition(value: Any?, propertyOperation: PropertyOperation): Boolean {
        val paramValue = value as Number? ?: return false
        when (propertyOperation.operationType) {
            OperationType.EQ -> {
                val conditionValue =
                    getConditionValue((propertyOperation as SingleValueOperation).value)
                return conditionValue?.let { it.toLong() == paramValue.toLong() } ?: false
            }
            OperationType.NEQ -> {
                val conditionValue =
                    getConditionValue((propertyOperation as SingleValueOperation).value)
                return conditionValue?.let { it.toLong() != paramValue.toLong() } ?: false
            }
            OperationType.GT -> {
                val conditionValue =
                    getConditionValue((propertyOperation as SingleValueOperation).value)
                return conditionValue?.let { paramValue.toLong() > it.toLong() } ?: false
            }
            OperationType.LT -> {
                val conditionValue =
                    getConditionValue((propertyOperation as SingleValueOperation).value)
                return conditionValue?.let { paramValue.toLong() < it.toLong() } ?: false
            }
            OperationType.GTE -> {
                val conditionValue =
                    getConditionValue((propertyOperation as SingleValueOperation).value)
                return conditionValue?.let { paramValue.toLong() >= it.toLong() } ?: false
            }
            OperationType.LTE -> {
                val conditionValue =
                    getConditionValue((propertyOperation as SingleValueOperation).value)
                return conditionValue?.let { paramValue.toLong() <= it.toLong() } ?: false
            }
            OperationType.BETWEEN -> {
                val doubleValueOperation = propertyOperation as BetweenOperation
                val fromValue = getConditionValue(doubleValueOperation.from)
                val toValue = getConditionValue(doubleValueOperation.to)
                return if (fromValue != null && toValue != null)
                    paramValue.toLong() >= fromValue.toLong() && paramValue.toLong() <= toValue.toLong()
                else false
            }
            else -> {
                logger.error("Operations type ${propertyOperation.propertyType} not supported for numeric operand")
                return false
            }
        }
    }

    private fun getConditionValue(value: String): Number? {
        return try {
            NumberFormat.getInstance().parse(value)
        } catch (e: ParseException) {
            return null
        }
    }
}