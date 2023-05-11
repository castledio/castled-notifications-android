package io.castled.notifications.trigger

import io.castled.notifications.trigger.EventFilterEvaluator.evaluate
import io.castled.notifications.trigger.models.*
import io.castled.notifications.workmanager.models.CastledInAppEventRequest
import io.castled.notifications.workmanager.models.CastledNetworkRequest
import io.castled.notifications.workmanager.models.CastledPushEventRequest
import io.castled.notifications.workmanager.models.CastledPushRegisterRequest
import junit.framework.TestCase
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.Test

class TriggerEvaluatorTest {

    @Test
    fun testTriggerEvaluation() {
        val trueParams: MutableMap<String, Any> = HashMap()
        trueParams["name"] = "ScreenA"
        val falseParams: MutableMap<String, Any> = HashMap()
        falseParams["name"] = "ScreenB"
        val triggerExpr = """{
    "type": "GROUP",
    "joinType": "OR",
    "filters": [
      {
        "type": "HAVING_PROPERTY",
        "name": "name",
        "operation": {
          "type": "EQ",
          "propertyType": "string",
          "value": "ScreenA"
        }
      }
    ]
  }"""

        val eventFilter: EventFilter = Json.decodeFromString(triggerExpr)
        TestCase.assertTrue(evaluate(eventFilter as GroupFilter, trueParams))
        TestCase.assertFalse(evaluate(eventFilter, falseParams))
    }
}