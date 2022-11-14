package io.castled.notifications.tasks;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import io.castled.notifications.exceptions.CastledApiException;
import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.service.CastledNotificationApi;
import io.castled.notifications.service.CastledNotificationService;
import io.castled.notifications.service.models.DeviceRegisterResponse;
import io.castled.notifications.service.models.ErrorResponse;
import io.castled.notifications.service.models.FcmDeviceRegisterRequest;
import io.castled.notifications.service.models.UserUpdateRequest;
import io.castled.notifications.store.CastledInstancePrefStore;
import io.castled.notifications.store.consts.PrefStoreKeys;
import io.castled.notifications.tasks.models.CastledServerTask;
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
            case USERID_SET:
                processUserIdTask(serverTask);
                break;
            case TOKEN_REGISTER:
                processTokenRegister(serverTask);
                break;
            default:
                logger.error(String.format("Unhandled task type: %s", serverTask.getTaskType()));
        }
    }

    private void processTokenRegister(CastledServerTask serverTask) {

        logger.debug("processing token register task");

        synchronized (prefStore) {

            String instanceId = prefStore.getInstanceId();

            TokenUploadServerTask tokenUploadServerTask = (TokenUploadServerTask) serverTask;
            CastledNotificationApi castledNotificationApi = CastledNotificationService.getCastledNotificationApi(instanceId);
            FcmDeviceRegisterRequest registerRequest = new FcmDeviceRegisterRequest(tokenUploadServerTask.getFcmToken());

            prefStore.put(PrefStoreKeys.PREF_KEY_FCM_TOKEN_UNREGISTERED, tokenUploadServerTask.getFcmToken());

            String currentFcmToken = prefStore.get(PrefStoreKeys.PREF_KEY_FCM_TOKEN);
            if (currentFcmToken != null && currentFcmToken.equals(registerRequest.getFcmToken())) {
                logger.debug(String.format("Token: %s unchanged, skipping token register!", currentFcmToken));
                prefStore.remove(PrefStoreKeys.PREF_KEY_FCM_TOKEN_UNREGISTERED);
                return;
            }

            DeviceRegisterResponse deviceRegisterResponse;
            Response<DeviceRegisterResponse> response;

            try {

                response = castledNotificationApi.registerToken(instanceId, registerRequest).execute();

                if (!response.isSuccessful()) {
                    handleErrorResponse(response);
                }
                else {

                    deviceRegisterResponse = response.body();
                    prefStore.put(PrefStoreKeys.PREF_KEY_ANON_ID, deviceRegisterResponse.getAnonId());
                    prefStore.put(PrefStoreKeys.PREF_KEY_FCM_TOKEN, registerRequest.getFcmToken());
                    prefStore.remove(PrefStoreKeys.PREF_KEY_FCM_TOKEN_UNREGISTERED);
                    logger.debug("token registered");

                    String unregisteredUserId = prefStore.get(PrefStoreKeys.PREF_KEY_USER_ID_UNREGISTERED);
                    if(unregisteredUserId != null) {
                        UserIdSetTask task = new UserIdSetTask(unregisteredUserId);
                        taskQueue.add(task);
                    }
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

            String instanceId = prefStore.getInstanceId();
            String anonId = prefStore.get(PrefStoreKeys.PREF_KEY_ANON_ID);

            UserIdSetTask userIdSetTask = (UserIdSetTask) serverTask;

            prefStore.put(PrefStoreKeys.PREF_KEY_USER_ID_UNREGISTERED, userIdSetTask.getUserId());
            String currentUserId = prefStore.get(PrefStoreKeys.PREF_KEY_USER_ID);
            String unregisteredToken = prefStore.get(PrefStoreKeys.PREF_KEY_FCM_TOKEN_UNREGISTERED);

            if(unregisteredToken != null) {

                logger.debug("Token not updated to server, skipping Id registration!");
            }
            else if (anonId == null) {

                logger.debug("Anon id not set!");
                checkIfTokenIsGenerated();
            }
            else if (currentUserId != null && currentUserId.equals(userIdSetTask.getUserId())) {

                logger.debug("UserId already set!");
                prefStore.remove(PrefStoreKeys.PREF_KEY_USER_ID_UNREGISTERED);
            }
            else {

                CastledNotificationApi castledNotificationApi = CastledNotificationService.getCastledNotificationApi(instanceId);
                UserUpdateRequest userUpdateRequest = new UserUpdateRequest(userIdSetTask.getUserId(), anonId);

                try {

                    Response<Void> response = castledNotificationApi.setUserId(instanceId, userUpdateRequest).execute();
                    if (!response.isSuccessful()) {
                        handleErrorResponse(response);
                    }

                    prefStore.put(PrefStoreKeys.PREF_KEY_USER_ID, userIdSetTask.getUserId());
                    prefStore.remove(PrefStoreKeys.PREF_KEY_USER_ID_UNREGISTERED);
                    logger.debug("UserId set!");
                }
                catch (IOException e) {

                    throw new CastledApiException(e.getMessage());
                }
            }
        }
    }

    private void checkIfTokenIsGenerated() {

        try {

            logger.debug("Check if token is generated...");

            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {

                if (!task.isSuccessful()) {
                    logger.warning("No token generated yet!");
                    return;
                }

                TokenUploadServerTask tokenUploadServerTask = new TokenUploadServerTask(task.getResult());
                taskQueue.add(tokenUploadServerTask);
            });
        }
        catch (Exception e) {

             e.printStackTrace();
        }
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
