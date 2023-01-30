package io.castled.notifications.trigger.models;

import io.castled.notifications.trigger.enums.OperationType;
import io.castled.notifications.trigger.enums.PropertyType;

public class SingleValueOperation extends PropertyOperation {

    private String value;

    public SingleValueOperation(String value, OperationType operationType, PropertyType propertyType) {
        super(operationType, propertyType);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
