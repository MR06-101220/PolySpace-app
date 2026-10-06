package com.polyspace.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Promo(
    val name: String,
    val url: String
)