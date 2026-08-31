package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface AladhanApi {
    @GET("v1/timings")
    suspend fun getTimings(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 2
    ): AladhanResponse
}
