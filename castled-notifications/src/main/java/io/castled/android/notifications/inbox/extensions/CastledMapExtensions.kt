package io.castled.android.notifications.inbox.extensions

import io.castled.android.notifications.commons.toMapString
import io.castled.android.notifications.push.models.CastledActionContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


internal fun Map<String, Any>.toCastledActionContext() =
    CastledActionContext(
        actionType = ((this["clickAction"] as? JsonPrimitive?)?.content
            ?: (this["clickAction"] as? String) ?: "NONE").toCastledClickAction(),
        actionLabel = (this["label"] as? JsonPrimitive?)?.content ?: this["label"] as? String ?: "",
        actionUri = (this["url"] as? JsonPrimitive?)?.content ?: (this["url"] as? String)
        ?: "",
        keyVals = ((this["keyVals"] as? JsonObject)?.toMapString()),
    )

