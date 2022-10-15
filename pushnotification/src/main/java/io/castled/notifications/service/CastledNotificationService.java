package io.castled.notifications.service;

public class CastledNotificationService {

    private static CastledNotificationApi castledNotificationApi;

    public static synchronized CastledNotificationApi getCastledNotificationApi(String instanceId) {
        if (castledNotificationApi == null) {
            castledNotificationApi = CastledRetrofitClient.getInstance(instanceId)
                    .create(CastledNotificationApi.class);
        }
        return castledNotificationApi;
    }

}
