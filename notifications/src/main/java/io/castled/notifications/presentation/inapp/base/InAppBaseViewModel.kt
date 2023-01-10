package io.castled.notifications.presentation.inapp.base

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import io.castled.notifications.data.dto.model.InAppModel
import io.castled.notifications.presentation.base.Interactor
import io.castled.notifications.presentation.base.OnBackPressed
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

open class InAppBaseViewModel(private val inAppModel: InAppModel) : ViewModel() {

    private val interactionSubject: Subject<Interactor> = PublishSubject.create()
    val interactions: Observable<Interactor> = interactionSubject.hide()

    val contentImage = ObservableField(inAppModel.imageUrl)
    val title = ObservableField(inAppModel.title)
    val message = ObservableField(inAppModel.body)

    val showButtons = ObservableBoolean(inAppModel.actionButtons.isNotEmpty())
    val hasSecondaryButton = ObservableBoolean(inAppModel.actionButtons.size > 1)

    val primaryButtonText = ObservableField("")
    val primaryButtonTextColor = ObservableField("")
    val primaryButtonColor = ObservableField("")
    val primaryButtonBorderColor = ObservableField("")

    val secondaryButtonText = ObservableField("")
    val secondaryButtonTextColor = ObservableField("")
    val secondaryButtonColor = ObservableField("")
    val secondaryButtonBorderColor = ObservableField("")

    init {

        if(showButtons.get()) {

            val actionButtons = inAppModel.actionButtons
            val primaryButton = actionButtons[0]
            primaryButtonText.set(primaryButton.label)
            primaryButtonTextColor.set(primaryButton.fontColor)
            primaryButtonColor.set(primaryButton.buttonColor)
            primaryButtonBorderColor.set(primaryButton.borderColor)

            if(actionButtons.size > 1) {

                val secondaryButton = actionButtons[1]
                secondaryButtonText.set(secondaryButton.label)
                secondaryButtonTextColor.set(secondaryButton.fontColor)
                secondaryButtonColor.set(secondaryButton.buttonColor)
                secondaryButtonBorderColor.set(secondaryButton.borderColor)
            }
        }
    }

    fun onPrimaryButtonClick() {

        if(showButtons.get()) {

            val actionButtons = inAppModel.actionButtons
            val primaryButton = actionButtons[0]
            handleClickAction(primaryButton.clickAction)
        }

        closeInAppNotification()
    }

    fun onSecondaryButtonClick() {

        closeInAppNotification()
    }

    fun closeInAppNotification() {

        println("closeInAppNotification")
    }

    private fun handleClickAction(clickedAction: String) {

        //Todo Refer PUSH action handling

        /*var clientIntent: Intent? = null

        if (clickedAction == ClickAction.DEEP_LINKING.name) { // see ClickAction class

            clientIntent = Intent()
            clientIntent.setAction(Intent.ACTION_VIEW)
            *//*clientIntent.setData(
                Uri.parse(
                    if (keyValuesMap != null) Utils.addQueryParams(
                        clickUri,
                        keyValuesMap
                    ) else clickUri
                )
            )*//*
        } else if (clickedAction == ClickAction.NAVIGATE_TO_SCREEN.name) {
            clientIntent = Intent(
                context,
                Class.forName(clickUri)
            ) // Class name should be fully qualified name
            if (keyValuesMap != null) clientIntent.putExtras(Utils.mapToBundle(keyValuesMap))
        } else if (clickedAction == ClickAction.RICH_LANDING.name) {
            clientIntent =
                context.getPackageManager().getLaunchIntentForPackage(context.getPackageName())
        } else if (clickedAction == ClickAction.DEFAULT.name) { //Default click action
            clientIntent =
                context.getPackageManager().getLaunchIntentForPackage(context.getPackageName())
        }*/
    }

    protected fun emitAction(command: Interactor) {
        interactionSubject.onNext(command)
    }

    fun onBackPressed() {
        emitAction(OnBackPressed)
    }
}