package io.castled.notifications.trigger;

import io.castled.notifications.trigger.models.PropertyOperation;

public interface ParamsConditionEvaluator {

    boolean evaluateCondition(Object value, PropertyOperation propertyOperation);
}
