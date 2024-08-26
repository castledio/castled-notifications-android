package io.castled.android.notifications.trigger

import io.castled.android.notifications.trigger.EventFilterEvaluator.evaluate
import io.castled.android.notifications.trigger.models.EventFilter
import io.castled.android.notifications.trigger.models.GroupFilter
import junit.framework.TestCase
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test

class TriggerEvaluatorTest {
    @After
    fun tearDown() {
    }

    @Before
    fun setUp() {
    }

    @Test
    fun testInappTriggerEvaluation() {
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