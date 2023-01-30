package io.castled.notifications.trigger.models;

public class PropertyFilter extends EventFilter {

    private String name;
    private PropertyOperation operation;

    public String getName() {
        return name;
    }

    public PropertyOperation getOperation() {
        return operation;
    }
}
