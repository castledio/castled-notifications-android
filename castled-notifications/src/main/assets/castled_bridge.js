class CastledBridge {

    dismissMessage(optional_params) {
        const jsonObject = {
            'clickAction': 'DISMISS_NOTIFICATION',
            'keyVals': optional_params
        };
        castledBridgeInternal.onButtonClicked(JSON.stringify(jsonObject));
    }
    navigateToScreen(screen_name, optional_params) {

        var jsonObject = {
            'clickActionUrl': screen_name,
            'clickAction': 'NAVIGATE_TO_SCREEN',
            'keyVals': optional_params
        };
        castledBridgeInternal.onButtonClicked(JSON.stringify(jsonObject));

    }
    openDeepLink(deeplink_url, optional_params) {
        var jsonObject = {
            'clickActionUrl': deeplink_url,
            'clickAction': 'DEEP_LINKING',
            'keyVals': optional_params
        };

        castledBridgeInternal.onButtonClicked(JSON.stringify(jsonObject));
    }

    openRichLanding(richlanding_url, optional_params) {

        var jsonObject = {
            'clickActionUrl': richlanding_url,
            'clickAction': 'RICH_LANDING',
            'keyVals': optional_params
        };
        castledBridgeInternal.onButtonClicked(JSON.stringify(jsonObject));

    }
    requestPushPermission(optional_params) {
        const jsonObject = {
            'clickAction': 'REQUEST_PUSH_PERMISSION',
            'keyVals': optional_params
        };
        castledBridgeInternal.onButtonClicked(JSON.stringify(jsonObject));
    }

    customAction(optional_params) {
        const jsonObject = {
            'clickAction': 'CUSTOM',
            'keyVals': optional_params
        };
        castledBridgeInternal.onButtonClicked(JSON.stringify(jsonObject));
    }
}
const castledBridge = new CastledBridge();