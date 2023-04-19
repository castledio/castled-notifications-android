package io.castled.notifications.trigger.models

import io.castled.notifications.trigger.enums.FilterType

sealed class EventFilter(val type: FilterType)