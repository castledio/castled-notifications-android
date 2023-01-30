package io.castled.notifications.trigger;

import io.castled.notifications.exceptions.CastledRuntimeException;
import io.castled.notifications.trigger.models.PropertyOperation;
import io.castled.notifications.trigger.models.SingleValueOperation;

public class BooleanConditionEvaluator implements ParamsConditionEvaluator {

    @Override
    public boolean evaluateCondition(Object value, PropertyOperation propertyOperation) {
        Boolean aBool = (Boolean) value;
        Boolean conditionValue = Boolean.valueOf(((SingleValueOperation) propertyOperation).getValue());
        switch (propertyOperation.getOperationType()) {
            case EQ:
                return aBool != null && conditionValue.booleanValue() == aBool.booleanValue();
            default:
                throw new CastledRuntimeException(String.format("Operations type %s not supported for boolean operand", propertyOperation.getPropertyType()));
        }
    }
}
