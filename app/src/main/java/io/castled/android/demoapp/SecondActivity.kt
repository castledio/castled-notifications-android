package io.castled.android.demoapp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import io.castled.android.demoapp.databinding.ActivitySecondBinding
import io.castled.android.notifications.CastledNotifications
import io.castled.android.notifications.CastledUserAttributes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding
    private var entries = emptyList<String>()
    private val defaultPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

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
            CastledNotifications.resumeInApp()
            CastledNotifications.logCustomAppEvent(
                this,
                "added_to_cart_android",
                null
            )

            val userAttributes = CastledUserAttributes()
// Predefined attributes
            userAttributes.setFirstName("John")
            userAttributes.setLastName("Doe")
            userAttributes.setCity("Sanfrancisco")
            userAttributes.setCountry("US")
            userAttributes.setEmail("jdoe@email.com")
            userAttributes.setGender("M")
            userAttributes.setPhone("+13156227533")
// Custom Attributes
            userAttributes.setCustomAttribute("prime_member", true)
            userAttributes.setCustomAttribute("occupation", "artist")

            CastledNotifications.setUserAttributes(this, userAttributes)

        }

        binding.btnLogCustomEventWithParam.setOnClickListener {
            val eventParams = mutableMapOf<String, Any>()
            eventParams["you"] = "can"
            eventParams["pass"] = false
            eventParams["orNumbers"] = 42
            eventParams["orDates"] = Date()
            CastledNotifications.logCustomAppEvent(
                this,
                "Android${getCurrentTimeFormatted("ddMMHHmmss")}",
                eventParams
            )
            CastledNotifications.stopInApp()

            /*  val userDetails = mutableMapOf<String, Any>()
              userDetails["fName"] = "Antony"
              userDetails["mName"] = "Joe"
              userDetails["lName"] = "Mathew"
              userDetails["age"] = 35
              userDetails["dob"] = Date()
              userDetails["gender"] = "M"
              userDetails["identity"] = 21022
              CastledNotifications.setUserProfile(userDetails)*/
        }

    }

    private fun getCurrentTimeFormatted(customPattern: String? = null): String {
        val patternToUse = customPattern ?: defaultPattern
        val dateFormat = SimpleDateFormat(patternToUse, Locale.US)
        return dateFormat.format(Date())
    }

}