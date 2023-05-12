package io.castled.android.notifications.trigger

import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.trigger.models.PropertyOperation

internal abstract class ParamsConditionEvaluator {

    val logger = CastledLogger.getInstance(LogTags.TRIGGER_EVAL)

    abstract fun evaluateCondition(value: Any?, propertyOperation: PropertyOperation): Boolean

}