package io.castled.notifications.trigger.models

import io.castled.notifications.trigger.enums.FilterType
import io.castled.notifications.trigger.enums.JoinType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class EventFilter(val type: FilterType)

@Serializable
@SerialName("GroupFilter")
class GroupFilter(val joinType: JoinType, val filters: List<EventFilter>?) :
    EventFilter(FilterType.GROUP)

@Serializable
@SerialName("PropertyFilter")
internal class PropertyFilter(val name: String, val operation: PropertyOperation) :
    EventFilter(FilterType.HAVING_PROPERTY)