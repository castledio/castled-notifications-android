package io.castled.notifications;

import androidx.annotation.NonNull;

public interface Logger {

    void verbose(@NonNull String message);

    void debug(@NonNull String message);

    void info(@NonNull String message);

    void warning(@NonNull String message);

    void error(@NonNull String message);

    void error(@NonNull String message, @NonNull Throwable throwable);
}