package io.castled.notifications.trigger.models;

import java.util.List;

import io.castled.notifications.trigger.enums.JoinType;

public class NestedEventFilter extends EventFilter {

    private JoinType joinType;
    private List<EventFilter> nestedFilters;

    public JoinType getJoinType() {
        return joinType;
    }

    public List<EventFilter> getNestedFilters() {
        return nestedFilters;
    }
}
