package io.castled.notifications

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.castled.inAppTriggerEvents.event.EventNotification

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)


        EventNotification.getInstance().observeLifecycle(this, lifecycle);
//        getInstance().observeLifecycle(this@SecondActivity)
    }
}