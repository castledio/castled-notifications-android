package io.castled.notifications.tasks;

public interface TaskQueueListener<T> {

    void onAdd(T task);

    void onRemove(T task);

    void onFlush(T task);

    void onEmpty();
}
