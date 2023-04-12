package io.castled.notifications.push.utils;

import android.os.Bundle;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Utils {

    public static String addQueryParams(String url, HashMap<String, String> qP) {

        if(url == null) {
            return null;
        }

        StringBuilder constructedUrl = new StringBuilder(url.trim());
        boolean isFirstItem = true;
        if(qP != null) {
            if(constructedUrl.toString().contains("?")) {
                if(constructedUrl.toString().endsWith("?")) {
                    constructedUrl = new StringBuilder(constructedUrl.substring(0, constructedUrl.length() - 1));
                }
                else {
                    isFirstItem = false;
                }
            }

            for (LinkedHashMap.Entry<String, String> entry : qP.entrySet()) {
                try {
                    String key = URLEncoder.encode(entry.getKey(), "UTF-8");
                    String value = URLEncoder.encode(entry.getValue(), "UTF-8");
                    if (isFirstItem) {
                        constructedUrl.append("?").append(key).append("=").append(value);
                        isFirstItem = false;
                    } else {
                        constructedUrl.append("&").append(key).append("=").append(value);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return constructedUrl.toString().trim();
    }

    public static Bundle mapToBundle(HashMap<String, String> keyVals) {
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : keyVals.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        return bundle;
    }
}
