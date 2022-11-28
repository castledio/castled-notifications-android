package io.castled.notifications.tasks;

public interface TaskQueue<T> {

    void add(T item);

    void remove();

    void flush();

    void empty();
}
