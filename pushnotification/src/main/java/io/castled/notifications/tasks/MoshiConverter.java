package io.castled.notifications.tasks;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.tape2.ObjectQueue;

import java.io.IOException;
import java.io.OutputStream;

import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;

/** Converter which uses Moshi to serialize instances of class T to disk. */
class MoshiConverter<T> implements ObjectQueue.Converter<T> {

    private final JsonAdapter<T> jsonAdapter;

    public MoshiConverter(Moshi moshi, Class<T> type) {
        this.jsonAdapter = moshi.adapter(type);
    }

    @Override public T from(byte[] bytes) throws IOException {
        return jsonAdapter.fromJson(new Buffer().write(bytes));
    }

    @Override public void toStream(T val, OutputStream os) throws IOException {
        try (BufferedSink sink = Okio.buffer(Okio.sink(os))) {
            jsonAdapter.toJson(sink, val);
        }
    }
}