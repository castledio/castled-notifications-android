package io.castled.notifications.trigger;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.castled.notifications.trigger.enums.FilterType;
import io.castled.notifications.trigger.enums.JoinType;
import io.castled.notifications.trigger.enums.OperationType;
import io.castled.notifications.trigger.enums.PropertyType;
import io.castled.notifications.trigger.models.BetweenOperation;
import io.castled.notifications.trigger.models.EventFilter;
import io.castled.notifications.trigger.models.NestedEventFilter;
import io.castled.notifications.trigger.models.PropertyFilter;
import io.castled.notifications.trigger.models.PropertyOperation;
import io.castled.notifications.trigger.models.SingleValueOperation;

public class EventFilterDeserializer implements JsonDeserializer<EventFilter> {

    @Override
    public EventFilter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserializeEventFilter(json);
    }

    private EventFilter deserializeEventFilter(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        FilterType filterType = FilterType.valueOf(jsonObject.get("type").getAsString());
        if (filterType == FilterType.NESTED) {
            return deserializeNestedFilter(json);
        } else if (filterType == FilterType.HAVING_PROPERTY) {
            return deserializePropertyFilter(json);
        } else {
            throw new JsonParseException("Unrecognized filter type!");
        }
    }

    private NestedEventFilter deserializeNestedFilter(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        JoinType joinType = JoinType.valueOf(jsonObject.get("joinType").getAsString());
        JsonArray nestedFiltersArray = jsonObject.get("nestedFilters").getAsJsonArray();

        List<EventFilter> eventFilters = new ArrayList<>();
        for (JsonElement element : nestedFiltersArray) {
            eventFilters.add(deserializeEventFilter(element));
        }
        return new NestedEventFilter(joinType, eventFilters);
    }

    private PropertyFilter deserializePropertyFilter(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        JsonElement operationElement = jsonObject.get("operation");
        PropertyOperation propertyOperation = deserializeOperation(operationElement);
        return new PropertyFilter(name, propertyOperation);
    }

    private PropertyOperation deserializeOperation(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        OperationType operationType = OperationType.valueOf(jsonObject.get("type").getAsString());
        PropertyType propertyType = PropertyType.valueOf(jsonObject.get("propertyType").getAsString());
        switch (operationType) {
            case EQ:
            case GT:
            case LT:
            case GTE:
            case LTE:
            case NEQ:
                String value = jsonObject.get("value").getAsString();
                return new SingleValueOperation(value, operationType, propertyType);
            case BETWEEN:
                String from = jsonObject.get("from").getAsString();
                String to = jsonObject.get("to").getAsString();
                return new BetweenOperation(from, to, operationType, propertyType);
            default:
                throw new JsonParseException("Unrecognized operation type!");
        }
    }
}