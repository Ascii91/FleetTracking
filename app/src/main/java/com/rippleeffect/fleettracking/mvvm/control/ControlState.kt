package com.rippleeffect.fleettracking.mvvm.control

import com.rippleeffect.fleettracking.model.TestModel

interface ControlState {

    sealed class ViewState {


        object LoadingError : ViewState()


    }

    sealed class ViewAction {
        object CloseApp : ViewAction()
    }


}