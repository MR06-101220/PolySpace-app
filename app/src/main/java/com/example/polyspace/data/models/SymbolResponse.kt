package com.example.polyspace.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SymbolResponse(
    @SerialName("symbol_id") val symbolId: String?,
    @SerialName("symbol_name") val symbolName: String?,
    val matches: List<SymbolMatch> = emptyList()
)

@Serializable
data class SymbolMatch(
    @SerialName("symbol_id") val symbolId: String,
    @SerialName("symbol_name") val symbolName: String,
    val score: Double,
    @SerialName("base_score") val baseScore: Double?,
    @SerialName("override_score") val overrideScore: Double?
)