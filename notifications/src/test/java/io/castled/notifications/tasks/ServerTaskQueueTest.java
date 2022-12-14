package io.castled.notifications.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import io.castled.notifications.tasks.models.TokenUploadServerTask;

public class ServerTaskQueueTest {

    private static final String dirName = "testdir";
    private static final String fileName = "test.dat";

    @Before
    public void setUp() {
        new File(dirName).mkdirs();
    }

    @Test
    public void testQueueOps() {
        ServerTaskQueue serverTaskQueue = new ServerTaskQueue(new File(dirName, fileName));

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
        File f = new File(dirName, fileName);
        f.delete();
        File d = new File(dirName);
        d.delete();
    }

}
