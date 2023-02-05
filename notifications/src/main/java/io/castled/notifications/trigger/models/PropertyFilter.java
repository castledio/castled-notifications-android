package io.castled.notifications.trigger.models;

import io.castled.notifications.trigger.enums.FilterType;

public class PropertyFilter extends EventFilter {

    private final String name;
    private final PropertyOperation operation;

    public PropertyFilter(String name, PropertyOperation propertyOperation) {
        super(FilterType.HAVING_PROPERTY);
        this.name = name;
        this.operation = propertyOperation;
    }

    public String getName() {
        return name;
    }

    public PropertyOperation getOperation() {
        return operation;
    }

}
