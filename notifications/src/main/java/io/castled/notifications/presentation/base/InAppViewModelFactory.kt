package io.castled.notifications.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.castled.notifications.data.dto.model.InAppModel
import io.castled.notifications.presentation.inapp.base.InAppBaseViewModel

class InAppViewModelFactory(private val inAppModel: InAppModel) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InAppBaseViewModel(inAppModel) as T
    }
}