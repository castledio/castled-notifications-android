package io.castled.notifications.logger;

import android.util.Log;

import androidx.annotation.NonNull;

import io.castled.notifications.Logger;

public class CastledLogger implements Logger {

    private final String tag;

    private CastledLogger(String tag) {
        this.tag = tag;
    }

    @Override
    public void verbose(@NonNull String message) {
        Log.v(tag, message);
    }

    @Override
    public void debug(@NonNull String message) {
        Log.d(tag, message);
    }

    @Override
    public void info(@NonNull String message) {
        Log.i(tag, message);
    }

    @Override
    public void warning(@NonNull String message) {
        Log.w(tag, message);
    }

    @Override
    public void error(@NonNull String message) {
        Log.e(tag, message);
    }

    @Override
    public void error(@NonNull String message, @NonNull Throwable throwable) {
        Log.v(tag, message, throwable);
    }

    public static CastledLogger getInstance(String tag) {
        return new CastledLogger(tag);
    }
}
