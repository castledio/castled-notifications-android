package io.castled.notifications.tasks;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;

import io.castled.notifications.exceptions.CastledApiException;
import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.service.CastledNotificationApi;
import io.castled.notifications.service.CastledNotificationService;
import io.castled.notifications.service.models.DeviceRegisterResponse;
import io.castled.notifications.service.models.FcmDeviceRegisterRequest;
import io.castled.notifications.service.models.UserUpdateRequest;
import io.castled.notifications.store.CastledInstancePrefStore;
import io.castled.notifications.store.consts.PrefStoreKeys;
import io.castled.notifications.tasks.models.CastledServerTask;
import io.castled.notifications.tasks.models.TokenUploadServerTask;
import io.castled.notifications.tasks.models.UserIdSetTask;
import retrofit2.Response;

public class ServerTaskHandler extends Handler {

    private final CastledInstancePrefStore prefStore;
    private final CastledLogger logger;
    private final ServerTaskQueue taskQueue;

    private ServerTaskHandler(Looper looper, ServerTaskQueue taskQueue) {
        super(looper);
        this.prefStore = CastledInstancePrefStore.getInstance();
        this.logger = CastledLogger.getInstance();
        this.taskQueue = taskQueue;
    }

    public static ServerTaskHandler getInstance(ServerTaskQueue taskQueue) {
        HandlerThread handlerThread = new HandlerThread("CastledServerTaskHandlerThread");
        return new ServerTaskHandler(handlerThread.getLooper(), taskQueue);
    }

    @Override
    public void handleMessage(Message msg) {
        CastledServerTask serverTask = taskQueue.peek();
        if (serverTask != null) {
            processTask(serverTask);
            taskQueue.remove();
        }
    }

    private void processTask(CastledServerTask serverTask) {
        switch (serverTask.getTaskType()) {
            case USERID_SET:
                processUserIdTask(serverTask);
            case TOKEN_REGISTER:
                processTokenRegister(serverTask);
            default:
                logger.error(String.format("Unhandled task type: %s", serverTask.getTaskType()));
        }
    }

    private void processTokenRegister(CastledServerTask serverTask) {
        logger.debug("processing token register task");
        String instanceId = prefStore.getInstanceId();
        TokenUploadServerTask tokenUploadServerTask = (TokenUploadServerTask) serverTask;
        CastledNotificationApi castledNotificationApi = CastledNotificationService.getCastledNotificationApi(instanceId);
        FcmDeviceRegisterRequest registerRequest = new FcmDeviceRegisterRequest(tokenUploadServerTask.getFcmToken());

        DeviceRegisterResponse deviceRegisterResponse;
        Response<DeviceRegisterResponse> response;
        try {
            response = castledNotificationApi.registerToken(instanceId, registerRequest).execute();
            if (response.isSuccessful()) {
                deviceRegisterResponse = response.body();
                prefStore.put(PrefStoreKeys.PREF_KEY_ANON_ID, deviceRegisterResponse.getAnonId());
            } else {
                if (response.errorBody() != null) {
                    throw new CastledApiException(response.errorBody().toString());
                }
            }
        } catch (IOException e) {
            throw new CastledApiException(e.getMessage());
        }
    }

    private void processUserIdTask(CastledServerTask serverTask) {
        logger.debug("processing user id task");
        String instanceId = prefStore.getInstanceId();
        String anonId = prefStore.get(PrefStoreKeys.PREF_KEY_ANON_ID);
        UserIdSetTask userIdSetTask = (UserIdSetTask) serverTask;
        CastledNotificationApi castledNotificationApi = CastledNotificationService.getCastledNotificationApi(instanceId);
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest(userIdSetTask.getUserId(), anonId);
        try {
            Response<Void> response = castledNotificationApi.setUserId(instanceId, userUpdateRequest).execute();
            if (!response.isSuccessful()) {
                if (response.errorBody() != null) {
                    throw new CastledApiException(response.errorBody().toString());
                }
                throw new CastledApiException("Unknown error invoking Castled api!");
            }
        } catch (IOException e) {
            throw new CastledApiException(e.getMessage());
        }
    }

}
