package io.castled.android.demoapp

import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import io.castled.android.notifications.CastledNotifications
import io.castled.android.demoapp.databinding.ActivitySecondBinding
import java.text.SimpleDateFormat
import java.util.*

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding
    private var entries = emptyList<String>()
    private  val defaultPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

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
            CastledNotifications.logCustomAppEvent(this, "wo_params_android", null)
        }

        binding.btnLogCustomEventWithParam.setOnClickListener {
            val eventParams = mutableMapOf<String, Any>()
            eventParams["you"] = "can"
            eventParams["pass"] = false
            eventParams["orNumbers"] = 42
            eventParams["orDates"] = Date()
            CastledNotifications.logCustomAppEvent(this, "android event - ${getCurrentTimeFormatted("dd- MM HH:mm:ss")}", eventParams)
        }

    }
    private fun getCurrentTimeFormatted(customPattern: String? = null): String {
        val patternToUse = customPattern ?: defaultPattern
        val dateFormat = SimpleDateFormat(patternToUse, Locale.US)
        return dateFormat.format(Date())
    }
    override fun onResume() {
        super.onResume()
        CastledNotifications.logAppPageViewEvent(this, "SecondActivity")

    }
}