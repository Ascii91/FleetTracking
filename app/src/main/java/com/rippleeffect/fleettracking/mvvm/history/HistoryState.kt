package com.rippleeffect.fleettracking.mvvm.history

import com.rippleeffect.fleettracking.model.LocationRecord

interface HistoryState {

    sealed class ViewState {

        object Loading : ViewState()
        class DataLoaded(val items: List<LocationRecord>) : ViewState()
        object LoadingError : ViewState()


    }

    sealed class ViewAction {
        object CloseApp : ViewAction()

    }


}