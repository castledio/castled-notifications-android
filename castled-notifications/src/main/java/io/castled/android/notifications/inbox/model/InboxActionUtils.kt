package io.castled.android.notifications.inbox.model

import io.castled.android.notifications.commons.toMapString
import io.castled.android.notifications.push.models.CastledActionContext
import io.castled.android.notifications.push.models.CastledClickAction
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal object InboxActionUtils {

    private fun getCastledClickAction(action: String): CastledClickAction {
        return try {
            action.let { CastledClickAction.valueOf(action) }
        } catch (e: Exception) {
            CastledClickAction.NONE
        }
    }

    fun getCastledActionContextFromActionParams(actionParams: Map<String, Any>): CastledActionContext {
        return CastledActionContext(
            actionType = getCastledClickAction(
                ((actionParams["clickAction"] as? JsonPrimitive?)?.content
                    ?: (actionParams["clickAction"] as? String) ?: "NONE")
            ),
            actionLabel = (actionParams["label"] as? JsonPrimitive?)?.content
                ?: actionParams["label"] as? String
                ?: "",
            actionUri = (actionParams["url"] as? JsonPrimitive?)?.content
                ?: (actionParams["url"] as? String)
                ?: "",
            keyVals = ((actionParams["keyVals"] as? JsonObject)?.toMapString()),
        )
    }
}