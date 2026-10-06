package com.polyspace.app.data.remote

import com.polyspace.app.data.models.AdeResource
import com.polyspace.app.data.models.CourseEvent
import com.polyspace.app.data.models.Promo
import com.polyspace.app.data.models.SymbolResponse
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query
import com.polyspace.app.data.models.AdeTreeNode

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

    // To get Symbol for Courses
    @GET("/symbol/fetch")
    suspend fun fetchSymbol(
        @Query("courseName") courseName: String,
        @Query("community_weight") communityWeight: Double = 0.25,
        @Query("topK") topK: Int = 3
    ): SymbolResponse

    // To update symbol
    @PUT("/symbol/update")
    suspend fun updateSymbol(
        @Query("courseName") courseName: String,
        @Query("symbolId") symbolId: String
    ): SymbolResponse

    // To get ade Tree
    @GET("ade/tree")
    suspend fun getAdeTree(
        @Query("config") config: String? = null
    ): List<AdeTreeNode>

}