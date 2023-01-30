package io.castled.notifications.trigger;

import java.text.NumberFormat;
import java.text.ParseException;

import io.castled.notifications.exceptions.CastledRuntimeException;
import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.trigger.models.BetweenOperation;
import io.castled.notifications.trigger.models.PropertyOperation;
import io.castled.notifications.trigger.models.SingleValueOperation;

public class NumberConditionEvaluator implements ParamsConditionEvaluator {
    @Override
    public boolean evaluateCondition(Object value, PropertyOperation propertyOperation) {
        Number numberValue = (Number) value;
        switch (propertyOperation.getOperationType()) {
            case EQ:
                Number conditionValue = getConditionValue(((SingleValueOperation) propertyOperation).getValue());
                return numberValue != null && conditionValue.longValue() == numberValue.longValue();
            case NEQ:
                if (numberValue == null) {
                    return true;
                }
                conditionValue = getConditionValue(((SingleValueOperation) propertyOperation).getValue());
                return conditionValue.longValue() != numberValue.longValue();
            case GT:
                conditionValue = getConditionValue(((SingleValueOperation) propertyOperation).getValue());
                return numberValue != null && conditionValue.doubleValue() < numberValue.doubleValue();
            case LT:
                conditionValue = getConditionValue(((SingleValueOperation) propertyOperation).getValue());
                return numberValue != null && conditionValue.doubleValue() > numberValue.doubleValue();
            case GTE:
                conditionValue = getConditionValue(((SingleValueOperation) propertyOperation).getValue());
                return numberValue != null && conditionValue.longValue() <= numberValue.longValue();
            case LTE:
                conditionValue = getConditionValue(((SingleValueOperation) propertyOperation).getValue());
                return numberValue != null && conditionValue.longValue() >= numberValue.longValue();
            case BETWEEN:
                BetweenOperation doubleValueOperation = ((BetweenOperation) propertyOperation);
                Number fromValue = getConditionValue(doubleValueOperation.getFrom());
                Number toValue = getConditionValue(doubleValueOperation.getTo());
                return numberValue != null && fromValue.doubleValue() < numberValue.doubleValue() && toValue.doubleValue() > numberValue.doubleValue();
            default:
                throw new CastledRuntimeException(String.format("Operations type %s not supported for numeric operand", propertyOperation.getPropertyType()));
        }
    }

    private Number getConditionValue(String value) {
        try {
            return NumberFormat.getInstance().parse(value);
        } catch (ParseException e) {
            CastledLogger.getInstance().error(String.format("Unable to parse %s as a number", value));
            throw new CastledRuntimeException(e);
        }
    }
}
