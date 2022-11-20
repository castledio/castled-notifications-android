package io.castled.notifications.tasks;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import io.castled.notifications.exceptions.CastledApiException;
import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.service.CastledNotificationApi;
import io.castled.notifications.service.CastledNotificationService;
import io.castled.notifications.service.models.ErrorResponse;
import io.castled.notifications.service.models.FcmDeviceRegisterRequest;
import io.castled.notifications.store.CastledInstancePrefStore;
import io.castled.notifications.store.consts.PrefStoreKeys;
import io.castled.notifications.tasks.models.CastledServerTask;
import io.castled.notifications.tasks.models.NotificationEventServerTask;
import io.castled.notifications.tasks.models.TokenUploadServerTask;
import io.castled.notifications.tasks.models.UserIdSetTask;
import okhttp3.ResponseBody;
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
        handlerThread.start();
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
            case TOKEN_REGISTER:
                processTokenRegister(serverTask);
                break;
            case USERID_SET:
                processUserIdTask(serverTask);
                break;
            case NOTIFICATION_EVENT:
                processNotificationEvent(serverTask);
                break;
            default:
                logger.error(String.format("Unhandled task type: %s", serverTask.getTaskType()));
        }
    }

    private void processTokenRegister(CastledServerTask serverTask) {

        logger.debug("processing token register task");

        synchronized (prefStore) {

            String instanceId = prefStore.getInstanceId();
            String userId = prefStore.getUserIdIfAvailable();

            TokenUploadServerTask tokenUploadServerTask = (TokenUploadServerTask) serverTask;
            CastledNotificationApi castledNotificationApi = CastledNotificationService.getCastledNotificationApi(instanceId);
            FcmDeviceRegisterRequest registerRequest = new FcmDeviceRegisterRequest(tokenUploadServerTask.getFcmToken(), userId);

            prefStore.put(PrefStoreKeys.PREF_KEY_FCM_TOKEN_UNREGISTERED, tokenUploadServerTask.getFcmToken());

            String currentFcmToken = prefStore.get(PrefStoreKeys.PREF_KEY_FCM_TOKEN);
            if (currentFcmToken != null && currentFcmToken.equals(registerRequest.getFcmToken())) {
                logger.debug(String.format("Token: %s unchanged, skipping token register!", currentFcmToken));
                prefStore.remove(PrefStoreKeys.PREF_KEY_FCM_TOKEN_UNREGISTERED);
                return;
            }

            try {

                Response<Void> response = castledNotificationApi.registerToken(instanceId, registerRequest).execute();

                if(response.isSuccessful() || response.code() == 204) {
                    onTokenRegistered(registerRequest.getFcmToken(), userId);
                }
                else {
                    handleErrorResponse(response);
                }
            }
            catch (IOException e) {
                throw new CastledApiException("Please check your network connection!");
            }
        }
    }

    private void processUserIdTask(CastledServerTask serverTask) {

        logger.debug("processing user id task");

        synchronized (prefStore) {

            UserIdSetTask userIdSetTask = (UserIdSetTask) serverTask;

            String userId = userIdSetTask.getUserId();
            String token = prefStore.getTokenIfAvailable();

            prefStore.put(PrefStoreKeys.PREF_KEY_USER_ID_UNREGISTERED, userId);
            String registeredUserId = prefStore.get(PrefStoreKeys.PREF_KEY_USER_ID);

            if(userId != null) {

                if (registeredUserId != null && registeredUserId.equals(userId)) {

                    logger.debug("UserId already set!");
                    prefStore.remove(PrefStoreKeys.PREF_KEY_USER_ID_UNREGISTERED);
                }
                else if(token != null) {

                    processTokenRegister(new TokenUploadServerTask(token));
                }
                else {

                    logger.debug("No token available, skipping user id registration!");
                }
            }
        }
    }

    private void processNotificationEvent(CastledServerTask serverTask) {

        logger.debug("processing notification event report task");

        synchronized (prefStore) {

            String instanceId = "test-99"; //prefStore.getInstanceId();
            NotificationEventServerTask eventServerTask = (NotificationEventServerTask) serverTask;
            CastledNotificationApi castledNotificationApi = CastledNotificationService.getCastledNotificationApi(instanceId);

            logger.debug("Reporting event: " + eventServerTask.getEvent().eventType + ", action type - "+ eventServerTask.getEvent().actionType);

            try {

                Response<Void> response = castledNotificationApi.reportEvent(instanceId, eventServerTask.getEvent()).execute();

                if(response.isSuccessful() || response.code() == 204) {

                    logger.debug("notification event reported");
                }
                else {

                    //handleErrorResponse(response);
                }
            }
            catch (IOException e) {
                throw new CastledApiException("Please check your network connection!");
            }
        }
    }

    private void onTokenRegistered(String token, String userId) {

        prefStore.put(PrefStoreKeys.PREF_KEY_FCM_TOKEN, token);
        prefStore.remove(PrefStoreKeys.PREF_KEY_FCM_TOKEN_UNREGISTERED);

        logger.debug("token registered");

        if(userId != null) {
            onUserIdRegistered(userId);
        }
    }

    private void onUserIdRegistered(String userId) {

        prefStore.put(PrefStoreKeys.PREF_KEY_USER_ID, userId);
        prefStore.remove(PrefStoreKeys.PREF_KEY_USER_ID_UNREGISTERED);
        logger.debug("UserId set!");
    }

    private void handleErrorResponse(Response response) {
        if (response.errorBody() != null) {
            ErrorResponse errorResponse = getErrorResponse(response.errorBody());
            logger.error(errorResponse.getMessage());
            throw new CastledApiException(errorResponse.getMessage());
        }
        throw new CastledApiException("Unknown error invoking Castled api!");
    }

    private ErrorResponse getErrorResponse(ResponseBody responseBody) {
        JsonAdapter<ErrorResponse> jsonAdapter = new Moshi.Builder().build().adapter(ErrorResponse.class);
        try {
            return jsonAdapter.fromJson(responseBody.source());
        } catch (IOException e) {
            return new ErrorResponse(0, "Unknown error invoking Castled api!");
        }
    }
}
