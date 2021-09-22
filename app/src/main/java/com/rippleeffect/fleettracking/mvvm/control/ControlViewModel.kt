package com.rippleeffect.fleettracking.mvvm.control

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rippleeffect.fleettracking.model.TestModel
import com.rippleeffect.fleettracking.mvvm.base.BaseViewModel
import com.rippleeffect.fleettracking.repository.FleetTrackerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ControlViewModel @Inject constructor(
    private val fleetTrackerRepository: FleetTrackerRepository,
    private val state: SavedStateHandle
) :
    BaseViewModel<ControlState.ViewState, ControlState.ViewAction>() {



    fun arePermissionsDisabled(): Boolean {
        return !fleetTrackerRepository.arePermissionGranted()

    }


    fun isAlarmEnabled():Boolean
    {
        return fleetTrackerRepository.isAlarmEnabled()
    }

    fun setAlarmEnabled(enabled:Boolean)
    {
        return fleetTrackerRepository.setAlarmEnabled(enabled)
    }

    fun isFilteringEnabled(): Boolean {
        return fleetTrackerRepository.isFilteringEnabled()
    }

    fun setFilteringEnabled(enabled: Boolean) {
        fleetTrackerRepository.setFilteringEnabled(enabled)
    }


}