package io.castled.notifications.trigger;

import io.castled.notifications.exceptions.CastledRuntimeException;
import io.castled.notifications.trigger.models.PropertyOperation;
import io.castled.notifications.trigger.models.SingleValueOperation;
import io.castled.notifications.trigger.models.BetweenOperation;

public class StringConditionEvaluator implements ParamsConditionEvaluator {

    @Override
    public boolean evaluateCondition(Object value, PropertyOperation propertyOperation) {
        String textValue = (String) value;
        switch (propertyOperation.getOperationType()) {
            case EQ:
                return ((SingleValueOperation) propertyOperation).getValue().equals(textValue);
            case NEQ:
                if (textValue == null) {
                    return true;
                }
                return !((SingleValueOperation) propertyOperation).getValue().equals(textValue);
            case GT:
                return textValue!= null && ((SingleValueOperation) propertyOperation).getValue().compareTo(textValue) < 0;
            case LT:
                return textValue!= null && ((SingleValueOperation) propertyOperation).getValue().compareTo(textValue) > 0;
            case GTE:
                return textValue!= null && ((SingleValueOperation) propertyOperation).getValue().compareTo(textValue) <= 0;
            case LTE:
                return textValue!= null && ((SingleValueOperation) propertyOperation).getValue().compareTo(textValue) >= 0;
            case BETWEEN:
                BetweenOperation doubleValueOperation = ((BetweenOperation) propertyOperation);
                return textValue!= null && doubleValueOperation.getFrom().compareTo(textValue) < 0 && doubleValueOperation.getTo().compareTo(textValue) > 0;
            default:
                throw new CastledRuntimeException(String.format("Operations type %s not supported for numeric operand", propertyOperation.getPropertyType()));
        }
    }
}

