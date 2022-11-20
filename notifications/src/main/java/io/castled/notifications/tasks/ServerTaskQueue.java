package io.castled.notifications.tasks;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory;
import com.squareup.tape2.ObjectQueue;
import com.squareup.tape2.QueueFile;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.tasks.models.CastledServerTask;
import io.castled.notifications.tasks.models.CastledServerTaskType;
import io.castled.notifications.tasks.models.NotificationEventServerTask;
import io.castled.notifications.tasks.models.TokenUploadServerTask;
import io.castled.notifications.tasks.models.UserIdSetTask;

public class ServerTaskQueue implements TaskQueue<CastledServerTask> {

    private final CastledLogger logger;
    private final ObjectQueue<CastledServerTask> queue;
    private TaskQueueListener<CastledServerTask> queueListener;

    @Inject
    public ServerTaskQueue(File taskFile) {
        logger = CastledLogger.getInstance();
        try {
            QueueFile queueFile = new QueueFile.Builder(taskFile).build();
            PolymorphicJsonAdapterFactory<CastledServerTask> polymorphicJsonAdapterFactory = PolymorphicJsonAdapterFactory
                    .of(CastledServerTask.class, "taskType")
                    .withSubtype(TokenUploadServerTask.class, CastledServerTaskType.TOKEN_REGISTER.toString())
                    .withSubtype(UserIdSetTask.class, CastledServerTaskType.USERID_SET.toString())
                    .withSubtype(NotificationEventServerTask.class, CastledServerTaskType.NOTIFICATION_EVENT.toString());
            Moshi moshi = new Moshi.Builder().add(polymorphicJsonAdapterFactory).build();
            MoshiConverter<CastledServerTask> moshiConverter = new MoshiConverter<>(moshi, CastledServerTask.class);
            this.queue = ObjectQueue.create(queueFile, moshiConverter);
        } catch (IOException e) {
            // TODO: cleanup
            logger.error("Failed to create queue file!", e);
            throw new RuntimeException(e);
        }
    }

    public void register(TaskQueueListener<CastledServerTask> queueListener) {
        this.queueListener = queueListener;
    }

    @Override
    public void add(CastledServerTask item) {
        try {
            queue.add(item);
            if (queueListener != null) {
                queueListener.onAdd(item);
            }
        } catch (IOException e) {
            logger.error("Adding task failed!", e);
        }
    }

    @Override
    public CastledServerTask peek() {
        CastledServerTask task = null;
        try {
            task = queue.peek();
        } catch (IOException e) {
            logger.error("Adding task failed!", e);
        }
        return task;
    }

    @Override
    public void remove() {
        try {
            CastledServerTask task = queue.peek();
            queue.remove();
            if (queueListener != null) {
                queueListener.onRemove(task);
            }
        } catch (IOException e) {
            logger.error("Removing task failed!", e);
        }
    }

    private void createDirIfNotExists(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }
}
