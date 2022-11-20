package io.castled.notifications.consts;

import java.util.HashMap;

public class ActionButton {

    public HashMap<String, String> keyVals;
    public String label;
    public String url;
    public String clickAction;

    //"[{"keyVals":{"key1":"val1","key2":"val2","key3":"val3"},"label":"Button DL","url":"app:\/\/open.my.app","clickAction":"DEEP_LINKING"},
    // {"keyVals":{"key5":"val5","key6":"val6","key4":"val4"},"label":"Button NS","url":"app:\/\/open.my.app","clickAction":"NAVIGATE_TO_SCREEN"},
    // {"keyVals":{"key9":"val9","key7":"val7","key8":"val8"},"label":"Button RL","url":"app:\/\/open.my.app","clickAction":"RICH_LANDING"}]"
}
