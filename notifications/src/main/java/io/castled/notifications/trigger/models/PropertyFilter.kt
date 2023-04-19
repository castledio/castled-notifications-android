package io.castled.notifications.trigger.models

import io.castled.notifications.trigger.enums.FilterType

internal class PropertyFilter(val name: String, val operation: PropertyOperation) :
    EventFilter(FilterType.HAVING_PROPERTY)