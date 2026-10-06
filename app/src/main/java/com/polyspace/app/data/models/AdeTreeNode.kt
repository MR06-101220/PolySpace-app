package com.polyspace.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AdeTreeNode(
    val id: String,
    val label: String,
    val childCount: Int = -1,
    val hasChildren: Boolean = false,
    val config: String? = null
)