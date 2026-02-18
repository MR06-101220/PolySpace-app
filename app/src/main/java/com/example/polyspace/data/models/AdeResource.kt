package com.example.polyspace.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdeResource(
    val id: String,

    // API return 'label' but 'name' is used in the app
    // @SerialName is used to change that
    @SerialName("label")
    val name: String,

    // Field not returned by the API but used in the app
    val type: String? = null,
    val path: String? = null,

    // Field returned by the API but not used in the app
    val childCount: Int? = null,
    val hasChildren: Boolean? = null,
    val config: String? = null
)