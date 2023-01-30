package io.castled.notifications.trigger.models;

import io.castled.notifications.trigger.enums.OperationType;
import io.castled.notifications.trigger.enums.PropertyType;

public class PropertyOperation {

    private final OperationType operationType;
    private final PropertyType propertyType;

    public PropertyOperation(OperationType operationType, PropertyType propertyType) {
        this.operationType = operationType;
        this.propertyType = propertyType;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

}
