package com.rippleeffect.fleettracking.mvvm.map

import com.rippleeffect.fleettracking.model.LocationRecord
import com.rippleeffect.fleettracking.model.MapLocationRecord
import com.rippleeffect.fleettracking.model.TestModel

interface MapState {

    sealed class ViewState {

        object Loading : ViewState()
        class DataLoaded(val items: List<MapLocationRecord>) : ViewState()
        class DatesLoaded(val items: List<Long>) : ViewState()
        object LoadingError : ViewState()


    }

    sealed class ViewAction {
        object CloseApp : ViewAction()
    }


}