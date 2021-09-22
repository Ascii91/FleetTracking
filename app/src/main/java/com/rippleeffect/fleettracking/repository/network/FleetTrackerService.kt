package com.rippleeffect.fleettracking.repository.network

import com.rippleeffect.fleettracking.model.TestModel
import retrofit2.Response
import retrofit2.http.GET

interface FleetTrackerService {

    @GET("/todos")
    suspend fun getTodos(): Response<List<TestModel>>

}

