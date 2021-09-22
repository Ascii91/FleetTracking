package com.rippleeffect.fleettracking.mvvm.map

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rippleeffect.fleettracking.model.LocationRecord
import com.rippleeffect.fleettracking.model.MapLocationRecord
import com.rippleeffect.fleettracking.mvvm.base.BaseViewModel
import com.rippleeffect.fleettracking.repository.FleetTrackerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MapViewModel @Inject constructor(
    private val fleetTrackerRepository: FleetTrackerRepository,
    private val state: SavedStateHandle
) :
    BaseViewModel<MapState.ViewState, MapState.ViewAction>() {

    fun loadLocationData(dateInMillis: Long) {

        showLoading()
        viewModelScope.launch {

            val records = fleetTrackerRepository.getLocationRecordsByDay(dateInMillis)
            locationDataChanged(filterRecords(records))

        }

    }

    private fun filterRecords(it: List<LocationRecord>): ArrayList<MapLocationRecord> {

        val filteredRecords = it.filter {
            it.latitude > 0 && it.longitude > 0
        }.reversed()


        val recordsList = ArrayList<MapLocationRecord>()
        var currentRecord: MapLocationRecord? = null

        filteredRecords.forEach {
            if (currentRecord == null) {
                currentRecord =
                    MapLocationRecord(it.latitude, it.longitude, it.accuracy, it.timeInMillis, 0)
            } else {
                //Ako je distanca manja od 10 metara onda ih sjedini

                val distance = FloatArray(1)
                Location.distanceBetween(
                    it.latitude,
                    it.longitude,
                    currentRecord!!.latitude,
                    currentRecord!!.longitude,
                    distance
                )

                //Da li se preklapaju
                if (distance[0] - currentRecord!!.accuracy - it.accuracy < if (fleetTrackerRepository.isFilteringEnabled()) 100 else 10) {
                    //Preklapaju se, sjedini ih
                    currentRecord!!.endTime = it.timeInMillis
                    if (currentRecord!!.accuracy > it.accuracy) {
                        currentRecord!!.accuracy = it.accuracy
                        currentRecord!!.latitude = it.latitude
                        currentRecord!!.longitude = it.longitude
                    }


                } else {
                    //Ne preklapaju se. dodaj curent record u listu
                    recordsList.add(currentRecord!!)
                    currentRecord = MapLocationRecord(
                        it.latitude,
                        it.longitude,
                        it.accuracy,
                        it.timeInMillis,
                        0
                    )
                }
            }
        }




        if (currentRecord != null) recordsList.add(currentRecord!!)

        return recordsList

    }

    private fun locationDataChanged(it: List<MapLocationRecord>) {
        _viewState.value = MapState.ViewState.DataLoaded(it)
    }


    private fun showLoading() {
        _viewState.value = MapState.ViewState.Loading
    }

    fun loadDateData() {


        showLoading()
        viewModelScope.launch {

            val times = fleetTrackerRepository.getAllDatesInMillis()
            datesLoaded(times)
        }

    }

    private fun datesLoaded(dates: List<Long>) {
        _viewState.value = MapState.ViewState.DatesLoaded(dates)
    }

    fun isFilteringDisabled(): Boolean {
        return !fleetTrackerRepository.isFilteringEnabled()

    }


}