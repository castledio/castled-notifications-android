package io.castled.android.notifications.commons.extenstions

import io.castled.android.notifications.commons.ClickActionParams
import io.castled.android.notifications.push.models.CastledActionContext


internal fun ClickActionParams.toCastledActionContext() =
    CastledActionContext(
        actionType = action,
        actionLabel = actionLabel,
        actionUri = uri,
        keyVals = keyVals,
    )

