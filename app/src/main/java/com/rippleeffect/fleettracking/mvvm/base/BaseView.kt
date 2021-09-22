package com.rippleeffect.fleettracking.mvvm.base

interface BaseView<T, T2> {

    fun processState(state: T)
    fun processAction(action: T2)
    fun subscribeToViewModel()

}
