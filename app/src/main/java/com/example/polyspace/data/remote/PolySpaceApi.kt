package com.example.polyspace.data.remote

import com.example.polyspace.data.models.AdeResource
import com.example.polyspace.data.models.CourseEvent
import com.example.polyspace.data.models.Promo
import retrofit2.http.GET
import retrofit2.http.Query

interface PolySpaceApi {
    // Get promos
    @GET("promos")
    suspend fun getPromos(): List<Promo>

    // Search a student or resource
    @GET("ade/search")
    suspend fun searchResources(
        @Query("query") query: String
    ): List<AdeResource>

    // Timetable by promo
    @GET("timetable/by-date")
    suspend fun getTimetableByPromo(
        @Query("promo") promoName: String,
        @Query("date") date: String,
        @Query("force") force : Boolean? = null //refresh
    ): List<CourseEvent>

    // Timetable by ID
    @GET("timetable/by-rid")
    suspend fun getTimetableById(
        @Query("rid") resourceId: String,
        @Query("date") date: String,
        @Query("force") force : Boolean? = null
    ): List<CourseEvent>
}