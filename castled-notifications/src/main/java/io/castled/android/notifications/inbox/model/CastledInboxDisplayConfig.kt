package io.castled.android.notifications.inbox.model

import java.io.Serializable

class CastledInboxDisplayConfig : Serializable {
    var emptyMessageViewText: String = "We have no updates. Please check again later."
    var emptyMessageViewTextColor: String = "#000000"
    var inboxViewBackgroundColor: String = "#ffffff"
    var navigationBarBackgroundColor: String = "#ffffff"
    var navigationBarTitle: String = "App Inbox"
    var navigationBarTitleColor: String = "#ffffff"
    var hideNavigationBar: Boolean = false
}