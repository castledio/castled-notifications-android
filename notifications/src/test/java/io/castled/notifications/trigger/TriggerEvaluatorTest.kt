package io.castled.notifications.trigger

import io.castled.notifications.trigger.EventFilterEvaluator.evaluate
import io.castled.notifications.trigger.models.EventFilter
import io.castled.notifications.trigger.models.GroupFilter
import junit.framework.TestCase
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.junit.Test

class TriggerEvaluatorTest {

    @Test
    fun testTriggerEvaluation() {
        val trueParams: MutableMap<String, Any> = HashMap()
        trueParams["name"] = "ScreenA"
        val falseParams: MutableMap<String, Any> = HashMap()
        falseParams["name"] = "ScreenB"
        val triggerExpr = """{
    "type": "NESTED",
    "joinType": "OR",
    "nestedFilters": [
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
        val json = Json {
            ignoreUnknownKeys = true
            serializersModule = SerializersModule {
                contextual(EventFilter::class, EventFilterDeserializer)
            }
        }
        val eventFilter: EventFilter = json.decodeFromString(triggerExpr)
        TestCase.assertTrue(evaluate(eventFilter as GroupFilter, trueParams))
        TestCase.assertFalse(evaluate(eventFilter, falseParams))
    }
}