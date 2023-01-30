package io.castled.notifications.trigger;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.trigger.enums.JoinType;
import io.castled.notifications.trigger.models.NestedEventFilter;
import io.castled.notifications.trigger.models.PropertyFilter;
import io.castled.notifications.trigger.enums.PropertyType;

public class TriggerParamsEvaluator {

    private static final Map<PropertyType, ParamsConditionEvaluator> paramsEvaluators;
    static {
        paramsEvaluators = new HashMap<>();
        paramsEvaluators.put(PropertyType.number, new NumberConditionEvaluator());
        paramsEvaluators.put(PropertyType.bool, new BooleanConditionEvaluator());
        paramsEvaluators.put(PropertyType.date, new DateConditionEvaluator());
        paramsEvaluators.put(PropertyType.string, new StringConditionEvaluator());
    }

    public boolean evaluate(Map<String, Object> params, NestedEventFilter eventFilter) {
        if (CollectionUtils.isEmpty(eventFilter.getNestedFilters())) {
            return true;
        }
        JoinType joinType = eventFilter.getJoinType();
        List<PropertyFilter> propertyFilters = eventFilter.getNestedFilters().stream()
                .map(schemaFilterRef -> (PropertyFilter) schemaFilterRef)
                .collect(Collectors.toList());
        if (joinType == JoinType.AND) {
            return evaluateAnd(propertyFilters, params);
        } else {
            return evaluateOr(propertyFilters, params);
        }
    }

    private boolean evaluateAnd(List<PropertyFilter> propertyFilters, Map<String, Object> properties) {
        for (PropertyFilter propertyFilter : propertyFilters) {
            if (!evaluatePropertyFilter(propertyFilter, properties)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateOr(List<PropertyFilter> propertyFilters, Map<String, Object> properties) {
        for (PropertyFilter propertyFilter : propertyFilters) {
            if (evaluatePropertyFilter(propertyFilter, properties)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluatePropertyFilter(PropertyFilter propertyFilter, Map<String, Object> properties) {
        ParamsConditionEvaluator paramsConditionEvaluator = paramsEvaluators.get(propertyFilter.getOperation().getPropertyType());
        if (paramsConditionEvaluator != null) {
            return  paramsConditionEvaluator.evaluateCondition(properties.get(propertyFilter.getName()),
                    propertyFilter.getOperation());
        }
        CastledLogger.getInstance().error(String.format("No evaluator defined for property type: %s",
                propertyFilter.getOperation().getPropertyType()));
        return false;
    }
}

