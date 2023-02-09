package io.castled.notifications

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.castled.inAppTriggerEvents.event.EventNotification
import io.castled.inAppTriggerEvents.event.TestTriggerEvents
import io.castled.notifications.databinding.ActivityMainBinding
import io.castled.notifications.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)


        EventNotification.getInstance().observeLifecycle(this, lifecycle);
//        getInstance().observeLifecycle(this@SecondActivity)

        binding.btnShowDbEvent.setOnClickListener {

            val text: String = binding.editText.text.toString()

            TestTriggerEvents.getInstance().showDbTriggerEventDialog(this, if (text.isNotEmpty()) text.toInt() else 0)
        }

    }
}