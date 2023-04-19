package io.castled.notifications.trigger.models

import io.castled.notifications.trigger.enums.FilterType
import io.castled.notifications.trigger.enums.JoinType

class GroupFilter(val joinType: JoinType, val filters: List<EventFilter>?) :
    EventFilter(FilterType.GROUP)