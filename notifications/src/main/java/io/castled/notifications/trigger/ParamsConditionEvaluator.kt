package io.castled.notifications.trigger

import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import io.castled.notifications.trigger.models.PropertyOperation

internal abstract class ParamsConditionEvaluator {

    val logger = CastledLogger.getInstance(LogTags.TRIGGER_EVAL)

    abstract fun evaluateCondition(value: Any?, propertyOperation: PropertyOperation): Boolean

}