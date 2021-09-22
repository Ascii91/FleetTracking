package com.rippleeffect.fleettracking.mvvm.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rippleeffect.fleettracking.model.LocationRecord
import com.rippleeffect.fleettracking.mvvm.base.BaseViewModel
import com.rippleeffect.fleettracking.repository.FleetTrackerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val fleetTrackerRepository: FleetTrackerRepository,
    private val state: SavedStateHandle
) :
    BaseViewModel<HistoryState.ViewState, HistoryState.ViewAction>() {


    private fun locationDataChanged(locationData: List<LocationRecord>) {
        _viewState.value = HistoryState.ViewState.DataLoaded(locationData)
        Timber.d("Location data hack %s", locationData.size)
    }

    fun loadData() {
        showLoading()
        viewModelScope.launch {
            fleetTrackerRepository.getAllLocationRecords().collect {
                locationDataChanged(it)
            }
        }

    }

    private fun showLoading() {
        _viewState.value = HistoryState.ViewState.Loading
    }


}