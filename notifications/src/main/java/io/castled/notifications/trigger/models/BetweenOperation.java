package io.castled.notifications.trigger.models;

import io.castled.notifications.trigger.enums.OperationType;
import io.castled.notifications.trigger.enums.PropertyType;

public class BetweenOperation extends PropertyOperation {

    public BetweenOperation(String from, String to, OperationType operationType, PropertyType propertyType) {
        super(operationType, propertyType );
        this.from = from;
        this.to = to;
    }

    private final String from;
    private final String to;

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
