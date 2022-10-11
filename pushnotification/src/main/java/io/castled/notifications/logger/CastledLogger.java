package io.castled.notifications.logger;

import android.util.Log;

import androidx.annotation.NonNull;

import io.castled.notifications.Logger;

public class CastledLogger implements Logger {

    public static final String TAG = "castled";
    private static final CastledLogger logger = new CastledLogger();

    @Override
    public void verbose(@NonNull String message) {
        Log.v(TAG, message);
    }

    @Override
    public void debug(@NonNull String message) {
        Log.d(TAG, message);
    }

    @Override
    public void info(@NonNull String message) {
        Log.i(TAG, message);
    }

    @Override
    public void warning(@NonNull String message) {
        Log.w(TAG, message);
    }

    @Override
    public void error(@NonNull String message) {
        Log.e(TAG, message);
    }

    @Override
    public void error(@NonNull String message, @NonNull Throwable throwable) {
        Log.v(TAG, message, throwable);
    }

    public static CastledLogger getInstance() {
        return logger;
    }
}
