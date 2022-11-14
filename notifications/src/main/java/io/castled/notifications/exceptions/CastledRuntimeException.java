package io.castled.notifications.exceptions;

public class CastledRuntimeException extends RuntimeException {

    public CastledRuntimeException(String message) {
        super(message);
    }

    public CastledRuntimeException(Throwable cause) {
        super(cause);
    }
}