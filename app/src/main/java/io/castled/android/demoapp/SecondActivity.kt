package io.castled.android.demoapp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import io.castled.android.notifications.CastledNotifications
import io.castled.android.demoapp.databinding.ActivitySecondBinding
import java.util.*

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding
    private var entries = emptyList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

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