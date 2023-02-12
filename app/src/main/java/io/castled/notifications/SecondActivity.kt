package io.castled.notifications

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.castled.inAppTriggerEvents.event.EventNotification
import io.castled.inAppTriggerEvents.event.TestTriggerEvents
import io.castled.notifications.databinding.ActivitySecondBinding
import java.util.*

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)


        EventNotification.getInstance().observeLifecycle(this, lifecycle, "SecondActivity")
//        getInstance().observeLifecycle(this@SecondActivity)

        binding.btnShowDbEvent.setOnClickListener {

            val text: String = binding.editText.text.toString()

            TestTriggerEvents.getInstance().showDbTriggerEventDialog(this, if (text.isNotEmpty()) text.toInt() else 0)
        }

        binding.btnLogCustomEvent.setOnClickListener {

            val eventParams = mutableMapOf<String, Any>()
            eventParams["you"] = "can"
            eventParams["pass"] = false
            eventParams["orNumbers"] = 42
            eventParams["orDates"] = Date()
            EventNotification.getInstance().logCustomEvent(this, "ScreenA", eventParams)
        }

        binding.btnLogCustomEventNameIn.setOnClickListener {
            val eventParams = mutableMapOf<String, Any>()
            eventParams["name"] = "ScreenA"
            eventParams["you"] = "can"
            eventParams["pass"] = false
            eventParams["orNumbers"] = 42
            eventParams["orDates"] = Date()
            EventNotification.getInstance().logCustomEventForEventNameInEventParam(this, eventParams)
        }

    }
}