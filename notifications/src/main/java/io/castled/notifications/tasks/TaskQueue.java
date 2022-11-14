package io.castled.notifications.tasks;

public interface TaskQueue<T> {

    void add(T item);

    T peek();

    void remove();
}
