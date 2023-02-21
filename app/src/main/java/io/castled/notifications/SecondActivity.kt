package io.castled.notifications

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.castled.CastledNotifications
import io.castled.inAppTriggerEvents.event.EventNotification
import io.castled.inAppTriggerEvents.event.TestTriggerEvents
import io.castled.notifications.databinding.ActivitySecondBinding
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "SecondActivity"
class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding
    private var events = HashMap<Long, JsonObject>()
    private var entries = emptyList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TestTriggerEvents.getInstance().fetchDbTriggerEvents(this){ jsonArray ->

            jsonArray.forEach { json ->
                events[json.asJsonObject["id"].asLong] = json.asJsonObject
            }

            entries = events.map {
                val jsonObject = JsonObject()
                jsonObject.addProperty("id", it.key)
                jsonObject.addProperty("notificationId", it.value.asJsonObject.get("notificationId").asInt)
                jsonObject.addProperty("type", it.value.asJsonObject.get("message").asJsonObject.get("type").asString)

                jsonObject.toString()
            }

            if (entries.isNotEmpty()){
                val entries2 = entries as MutableList<String>
                entries2.add(0, "Select Db Event")

                val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, entries)

                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                binding.spinnerDbEvent.prompt = "Select Db Event"
                binding.spinnerDbEvent.adapter = arrayAdapter

            }
        }

        binding.spinnerDbEvent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0){
                    val event = events[JsonParser.parseString(entries[position]).asJsonObject.get("id").asLong]

                    event?.let {
                        TestTriggerEvents.getInstance().showDbTriggerEventDialog(this@SecondActivity, event)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }



        binding.btnShowDbEvent.setOnClickListener {

            val text: String = binding.editText.text.toString()

            TestTriggerEvents.getInstance().showDbTriggerEventDialog(this, if (text.isNotEmpty()) text.toInt() else 0)
        }

        binding.btnLogCustomEvent.setOnClickListener {
            CastledNotifications.logEvent(this, "event_name")
        }

        binding.btnLogCustomEventWithParam.setOnClickListener {
            val eventParams = mutableMapOf<String, Any>()
            eventParams["you"] = "can"
            eventParams["pass"] = false
            eventParams["orNumbers"] = 42
            eventParams["orDates"] = Date()
            CastledNotifications.logEvent(this, "event_name", eventParams)
        }

    }
}