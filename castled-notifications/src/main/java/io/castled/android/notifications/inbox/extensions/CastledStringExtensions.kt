package io.castled.android.notifications.inbox.extensions

import io.castled.android.notifications.push.models.CastledClickAction


internal fun String.toCastledClickAction(): CastledClickAction {
    return try {
        this.let { CastledClickAction.valueOf(this) }
    } catch (e: Exception) {
        CastledClickAction.NONE
    }
}
