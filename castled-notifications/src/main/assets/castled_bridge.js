class CastledBridge {

    dismissMessage(optional_params) {
        const jsonObject = {
            'clickAction'    : 'DISMISS_NOTIFICATION',
            'params'    : optional_params
        };
        castledBridgeInternal.dismissMessage(JSON.stringify(jsonObject));
    }
    navigateToScreen(screen_name,optional_params){

        var jsonObject = {
            'clickActionUrl' : screen_name,
            'clickAction'    : 'NAVIGATE_TO_SCREEN',
            'params'          : optional_params
        };
        castledBridgeInternal.navigateToScreen(screen_name,JSON.stringify(jsonObject));

    }
    openDeepLink(deeplink_url,optional_params) {
        var jsonObject = {
            'clickActionUrl' : deeplink_url,
            'clickAction'    : 'DEEP_LINKING',
            'params'          : optional_params
        };

        castledBridgeInternal.openDeepLink(deeplink_url,JSON.stringify(jsonObject));
    }

    openRichLanding(richlanding_url,optional_params){

        var jsonObject = {
            'clickActionUrl' : richlanding_url,
            'clickAction'    : 'RICH_LANDING',
            'params'          : optional_params
        };
        castledBridgeInternal.openDeepLink(richlanding_url,JSON.stringify(jsonObject));

    }
    requestPushPermission(optional_params) {
        const jsonObject = {
            'clickAction'    : 'REQUEST_PUSH_PERMISSION',
            'params'    : optional_params
        };
        castledBridgeInternal.requestPushPermission(JSON.stringify(jsonObject));
    }

    customAction(optional_params) {
        const jsonObject = {
            'clickAction'  : 'CUSTOM',
            'params'    : optional_params
        };
        castledBridgeInternal.customAction(JSON.stringify(jsonObject));
    }
}
const castledBridge = new CastledBridge();
