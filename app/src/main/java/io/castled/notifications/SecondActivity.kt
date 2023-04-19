package io.castled.notifications

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import io.castled.notifications.databinding.ActivitySecondBinding
import io.castled.notifications.inapp.test.TestTriggerEvents
import java.util.*
import kotlin.collections.HashMap

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding
    private var events = HashMap<Long, JsonObject>()
    private var entries = emptyList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TestTriggerEvents.getInstance(this).fetchDbTriggerEvents(this) { _ ->


            entries = events.map {
                val jsonObject = JsonObject()
                jsonObject.addProperty("id", it.key)
                jsonObject.addProperty(
                    "notificationId",
                    it.value.asJsonObject.get("notificationId").asInt
                )
                jsonObject.addProperty(
                    "type",
                    it.value.asJsonObject.get("message").asJsonObject.get("type").asString
                )

                jsonObject.toString()
            }

            if (entries.isNotEmpty()) {
                val entries2 = entries as MutableList<String>
                entries2.add(0, "Select Db Event")

                val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, entries)

                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                binding.spinnerDbEvent.prompt = "Select Db Event"
                binding.spinnerDbEvent.adapter = arrayAdapter

            }
        }

        binding.spinnerDbEvent.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }


        binding.btnShowDbEvent.setOnClickListener {

            val text: String = binding.editText.text.toString()

            TestTriggerEvents.getInstance(this)
                .showDbTriggerEventDialog(this, if (text.isNotEmpty()) text.toInt() else 0)
        }

        binding.btnLogCustomEvent.setOnClickListener {
            CastledNotifications.logCustomAppEvent(this, "event_name", null)
        }

        binding.btnLogCustomEventWithParam.setOnClickListener {
            val eventParams = mutableMapOf<String, Any>()
            eventParams["you"] = "can"
            eventParams["pass"] = false
            eventParams["orNumbers"] = 42
            eventParams["orDates"] = Date()
            CastledNotifications.logCustomAppEvent(this, "event_name", eventParams)
        }

    }

    override fun onResume() {
        super.onResume()
        CastledNotifications.logAppPageViewEvent(this, "SecondActivity")

    }
}