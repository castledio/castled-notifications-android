package io.castled.push.trigger;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import io.castled.notifications.trigger.EventFilterDeserializer;
import io.castled.notifications.trigger.TriggerParamsEvaluator;
import io.castled.notifications.trigger.models.EventFilter;
import io.castled.notifications.trigger.models.NestedEventFilter;

public class TriggerEvaluatorTest {

    @Test
    public void testTriggerEvaluation() {
        TriggerParamsEvaluator triggerParamsEvaluator = new TriggerParamsEvaluator();
        Map<String, Object> trueParams = new HashMap<>();
        trueParams.put("name", "ScreenA");

        Map<String, Object> falseParams = new HashMap<>();
        falseParams.put("name", "ScreenB");

        String triggerExpr = "{\n" +
                "    \"type\": \"NESTED\",\n" +
                "    \"joinType\": \"OR\",\n" +
                "    \"nestedFilters\": [\n" +
                "      {\n" +
                "        \"type\": \"HAVING_PROPERTY\",\n" +
                "        \"name\": \"name\",\n" +
                "        \"operation\": {\n" +
                "          \"type\": \"EQ\",\n" +
                "          \"propertyType\": \"string\",\n" +
                "          \"value\": \"ScreenA\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }";

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(EventFilter.class, new EventFilterDeserializer())
                .create();
        EventFilter eventFilter = gson.fromJson(triggerExpr, EventFilter.class);
        assertTrue(triggerParamsEvaluator.evaluate(trueParams, (NestedEventFilter) eventFilter));
        assertFalse(triggerParamsEvaluator.evaluate(falseParams, (NestedEventFilter) eventFilter));
    }
}
