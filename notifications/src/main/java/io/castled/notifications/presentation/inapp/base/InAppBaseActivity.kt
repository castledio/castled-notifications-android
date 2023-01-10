package io.castled.notifications.presentation.inapp.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.castled.notifications.databinding.LayoutInAppModalBinding

class InAppBaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Todo -- Set the view model with proper data object and
        //link the view model to the layout

        val viewBinding = LayoutInAppModalBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    private fun extractPayload() {

    }
}