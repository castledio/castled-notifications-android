package io.castled.inappNotifications.trigger

data class PopupHeader(val header: String, val fontColor:String, val fontSize:Float, val backgroundColor:String)

data class PopupMessage(val message: String, val fontColor:String, val fontSize:Float, val backgroundColor:String)

data class PopupPrimaryButton(val buttonText: String, val fontColor:String, val buttonColor:String, val borderColor:String, val urlOnClick: String)

data class PopupSecondaryButton(val buttonText: String, val fontColor:String, val buttonColor:String, val borderColor:String, val urlOnClick: String)