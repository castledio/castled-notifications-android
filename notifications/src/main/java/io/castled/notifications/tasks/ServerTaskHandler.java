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
import io.castled.notifications.logger.LogTags;
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
    private static final CastledLogger logger = CastledLogger.getInstance(LogTags.PUSH);
    private final ServerTaskQueue taskQueue;

    private ServerTaskHandler(Looper looper, ServerTaskQueue taskQueue) {
        super(looper);
        this.prefStore = CastledInstancePrefStore.getInstance();
        this.taskQueue = taskQueue;
    }

    public static ServerTaskHandler getInstance(ServerTaskQueue taskQueue) {
        HandlerThread handlerThread = new HandlerThread("CastledServerTaskHandlerThread");
        handlerThread.start();
        return new ServerTaskHandler(handlerThread.getLooper(), taskQueue);
    }

    @Override
    public void handleMessage(Message msg) {

        int action = msg.what;
        CastledServerTask serverTask;

        switch (action) {

            case ServerTaskListener.QUEUE_ADD:
            case ServerTaskListener.QUEUE_FLUSH:
                serverTask = (CastledServerTask) msg.obj;
                if (serverTask != null && processTask(serverTask)) {
                    taskQueue.remove();
                    taskQueue.flush();
                }
                break;

            case ServerTaskListener.QUEUE_REMOVE:
                serverTask = (CastledServerTask) msg.obj;
                logger.debug("Task (" + serverTask.getTaskType().name() + ") successfully removed!");
                break;

            case ServerTaskListener.QUEUE_EMPTY:
                //In the event of logout
        }
    }

    private boolean processTask(CastledServerTask serverTask) {

        boolean isProcessed = false;

        switch (serverTask.getTaskType()) {
            case TOKEN_REGISTER:
                isProcessed = processTokenRegister(serverTask);
                break;
            case USERID_SET:
                isProcessed = processUserIdTask(serverTask);
                break;
            case NOTIFICATION_EVENT:
                isProcessed = processNotificationEvent(serverTask);
                break;
            default:
                logger.error(String.format("Unhandled task type: %s", serverTask.getTaskType()));
        }

        return isProcessed;
    }

    private boolean processTokenRegister(CastledServerTask serverTask) {

        logger.debug("processing token register task");

        boolean isApiSuccess = false;

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
                isApiSuccess = true;
            }
            else {

                CastledApiException exception = null;

                try {

                    Response<Void> response = castledNotificationApi.registerToken(instanceId, registerRequest).execute();
                    if(response.isSuccessful()) {
                        isApiSuccess = true;
                        onTokenRegistered(registerRequest.getFcmToken(), userId);
                    }
                    else {

                        exception = handleErrorResponse(response);
                    }
                }
                catch (Exception e) {
                    exception = new CastledApiException("Please check your network connection!");
                }

                if(exception != null && exception.getMessage() != null)
                    logger.error(exception.getMessage());
            }
        }

        return isApiSuccess;
    }

    private boolean processUserIdTask(CastledServerTask serverTask) {

        logger.debug("processing user id task");

        boolean isApiSuccess = false;

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
                    isApiSuccess = true;
                }
                else if(token != null) {

                    isApiSuccess = processTokenRegister(new TokenUploadServerTask(token));
                }
                else {

                    logger.debug("No token available, skipping user id registration!");
                    isApiSuccess = true;
                }
            }
        }

        return isApiSuccess;
    }

    private boolean processNotificationEvent(CastledServerTask serverTask) {

        logger.debug("processing notification event report task");

        boolean isApiSuccess = false;

        synchronized (prefStore) {

            String instanceId = prefStore.getInstanceId();
            NotificationEventServerTask eventServerTask = (NotificationEventServerTask) serverTask;
            CastledNotificationApi castledNotificationApi = CastledNotificationService.getCastledNotificationApi(instanceId);

            logger.debug("Reporting event: " + eventServerTask.getEvent().eventType + ", action type - "+ eventServerTask.getEvent().actionType);

            CastledApiException exception = null;

            try {

                Response<Void> response = castledNotificationApi.reportEvent(instanceId, eventServerTask.getEvent()).execute();

                if(response.isSuccessful()) {

                    isApiSuccess = true;
                    logger.debug("notification event reported");
                }
                else {

                    exception = handleErrorResponse(response);
                }
            }
            catch (Exception e) {
                exception = new CastledApiException("Please check your network connection!");
            }

            if(exception != null && exception.getMessage() != null)
                logger.error(exception.getMessage());
        }

        return isApiSuccess;
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

    private CastledApiException handleErrorResponse(Response response) {
        if (response != null && response.errorBody() != null) {
            ErrorResponse errorResponse = getErrorResponse(response.errorBody());
            logger.error(errorResponse.getMessage());
            return new CastledApiException(errorResponse.getMessage());
        }
        return new CastledApiException("Unknown error invoking Castled api!");
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
