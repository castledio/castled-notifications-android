package io.castled.notifications.logger

import android.util.Log

internal class CastledLogger private constructor(private val tag: String) : Logger {
    override fun verbose(message: String) {
        Log.v(tag, message)
    }

    override fun debug(message: String) {
        Log.d(tag, message)
    }

    override fun info(message: String) {
        Log.i(tag, message)
    }

    override fun warning(message: String) {
        Log.w(tag, message)
    }

    override fun error(message: String) {
        Log.e(tag, message)
    }

    override fun error(message: String, throwable: Throwable) {
        Log.e(tag, message, throwable)
    }

    companion object {
        fun getInstance(tag: String): CastledLogger {
            return CastledLogger(tag)
        }
    }
}