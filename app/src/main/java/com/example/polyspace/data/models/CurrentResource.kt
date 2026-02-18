package com.example.polyspace.data.models

// Get current resource
data class CurrentResource(
    val type: String,
    val id: String,
    val name: String,
    val isTemporary: Boolean = false
)