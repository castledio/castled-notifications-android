package io.castled.notifications.trigger.models;

import java.util.List;

import io.castled.notifications.trigger.enums.FilterType;
import io.castled.notifications.trigger.enums.JoinType;

public class NestedEventFilter extends EventFilter {

    private JoinType joinType;
    private List<EventFilter> nestedFilters;

    public NestedEventFilter(JoinType joinType, List<EventFilter> nestedFilters) {
        super(FilterType.NESTED);
        this.joinType = joinType;
        this.nestedFilters = nestedFilters;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public List<EventFilter> getNestedFilters() {
        return nestedFilters;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    public void setNestedFilters(List<EventFilter> nestedFilters) {
        this.nestedFilters = nestedFilters;
    }
}

