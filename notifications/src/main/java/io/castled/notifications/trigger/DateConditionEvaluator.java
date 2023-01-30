package io.castled.notifications.trigger;

import io.castled.notifications.exceptions.CastledRuntimeException;
import io.castled.notifications.trigger.models.BetweenOperation;
import io.castled.notifications.trigger.models.PropertyOperation;
import io.castled.notifications.trigger.models.SingleValueOperation;

public class DateConditionEvaluator implements ParamsConditionEvaluator {

    @Override
    public boolean evaluateCondition(Object value, PropertyOperation propertyOperation) {

        String dateText = (String) value;
        switch (propertyOperation.getOperationType()) {
            case BETWEEN:
                BetweenOperation dateInterval = (BetweenOperation) propertyOperation;
                return dateText != null && dateInterval.getFrom().compareTo(dateText) < 0 && dateInterval.getTo().compareTo(dateText) > 0;

            case EQ:
                SingleValueOperation dateEquals = (SingleValueOperation) propertyOperation;
                return dateEquals.getValue().equals(dateText);

            case NEQ:
                if (dateText == null) {
                    return true;
                }
                SingleValueOperation dateNotEquals = (SingleValueOperation) propertyOperation;
                return !dateNotEquals.getValue().equals(dateText);

            default:
                throw new CastledRuntimeException(String.format("Operations type %s not supported for timestamp operand", propertyOperation.getPropertyType()));
        }
    }
}
