package io.castled.notifications.tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;

import io.castled.notifications.tasks.models.TokenUploadServerTask;

public class ServerTaskQueueTest {

    private File qFile;

    @Before
    public void setUp() {
        qFile = new File("testq.dat");
    }

    @Test
    public void testQueueOps() {
        ServerTaskQueue serverTaskQueue = new ServerTaskQueue(qFile);

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

    @After
    public void tearDown() {
        qFile.delete();
    }

}
