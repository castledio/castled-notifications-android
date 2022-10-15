package io.castled.notifications.tasks;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.castled.notifications.tasks.models.TokenUploadServerTask;

public class ServerTaskQueueTest {

    private static final String dirName = "testdir";
    private static final String fileName = "test.dat";

    @Test
    public void testQueueOps() {
        ServerTaskQueue serverTaskQueue = new ServerTaskQueue(dirName, fileName);

        // Add
        for (int i = 1; i <= 10000; ++i) {
            TokenUploadServerTask task = new TokenUploadServerTask("token:" + i);
            serverTaskQueue.add(task);
        }

        // Peek & remove
        for (int i = 1; i <= 10000; ++i) {
            TokenUploadServerTask peekTask = (TokenUploadServerTask) serverTaskQueue.peek();
            assertEquals("token:" + i, peekTask.getFcmToken());
            serverTaskQueue.remove();
        }

        // Confirm empty
        assertNull(serverTaskQueue.peek());
    }

}
