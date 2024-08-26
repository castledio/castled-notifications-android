package io.castled.android.helpers

import android.app.Application
import androidx.test.core.app.ApplicationProvider

class CastledTestApplication {
    companion object {

        val application: Application
            get() = ApplicationProvider.getApplicationContext<Application>() as Application
    }
}