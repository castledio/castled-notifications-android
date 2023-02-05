package io.castled.notifications.trigger.models;

import io.castled.notifications.trigger.enums.FilterType;

public class EventFilter {

    private FilterType type;

    EventFilter(FilterType type) {
        this.type = type;
    }

    public void setType(FilterType type) {
        this.type = type;
    }

    public FilterType getType() {
        return type;
    }
}
