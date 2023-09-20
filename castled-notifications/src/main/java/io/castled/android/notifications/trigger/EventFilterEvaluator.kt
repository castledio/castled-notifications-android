package io.castled.android.notifications.trigger

import io.castled.android.notifications.trigger.enums.JoinType
import io.castled.android.notifications.trigger.enums.PropertyType
import io.castled.android.notifications.trigger.models.GroupFilter
import io.castled.android.notifications.trigger.models.PropertyFilter

internal object EventFilterEvaluator {

    private val paramsEvaluators = mutableMapOf(
        PropertyType.number to NumberConditionEvaluator(),
        PropertyType.bool to BooleanConditionEvaluator(),
        PropertyType.date to DateConditionEvaluator(),
        PropertyType.string to StringConditionEvaluator()
    )
     fun evaluate(eventFilter: GroupFilter, params: Map<String, Any>?): Boolean {
         if (eventFilter.filters == null || eventFilter.filters.isEmpty()) {
             return true
         }
         if (params == null) {
             return false
         }
         val propertyFilters = eventFilter.filters.map { it as PropertyFilter }
         return if (eventFilter.joinType === JoinType.AND) {
             evaluateAnd(propertyFilters, params)
         } else {
            evaluateOr(propertyFilters, params)
        }
    }

    private fun evaluateAnd(
        propertyFilters: List<PropertyFilter>,
        properties: Map<String, Any>
    ): Boolean {
        for (propertyFilter in propertyFilters) {
            if (!evaluatePropertyFilter(propertyFilter, properties)) {
                return false
            }
        }
        return true
    }

    private fun evaluateOr(
        propertyFilters: List<PropertyFilter>,
        properties: Map<String, Any>
    ): Boolean {
        for (propertyFilter in propertyFilters) {
            if (evaluatePropertyFilter(propertyFilter, properties)) {
                return true
            }
        }
        return false
    }

    private fun evaluatePropertyFilter(
        propertyFilter: PropertyFilter,
        params: Map<String, Any>
    ): Boolean {
        val paramsConditionEvaluator = paramsEvaluators[propertyFilter.operation.propertyType]
        return paramsConditionEvaluator?.evaluateCondition(
            params[propertyFilter.name],
            propertyFilter.operation
        )
            ?: false
    }
}