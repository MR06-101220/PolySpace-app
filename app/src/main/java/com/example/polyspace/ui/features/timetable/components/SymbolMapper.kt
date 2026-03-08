package com.example.polyspace.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

object SymbolMapper {
    fun getMaterialIcon(courseName: String, sfSymbolId: String?): ImageVector {
        val lowerName = courseName.lowercase()

        if (lowerName.contains("math")) return Icons.Outlined.Functions
        if (lowerName.contains("base de données") || lowerName.contains("bdd")) return Icons.Outlined.Storage
        if (lowerName.contains("anglais") || lowerName.contains("toeic")) return Icons.Outlined.Language
        if (lowerName.contains("onde") || lowerName.contains("oem") || lowerName.contains("électromagnétique") || lowerName.contains("electromagnetique")) return Icons.Outlined.GraphicEq
        if (lowerName.contains("vision") || lowerName.contains("robot")) return Icons.Outlined.SmartToy

        if (sfSymbolId == null) return Icons.Outlined.Book

        return when (sfSymbolId.lowercase()) {
            "atom" -> Icons.Outlined.Science
            "thermometer.snowflake" -> Icons.Outlined.AcUnit
            "waveform" -> Icons.Outlined.GraphicEq
            "water.waves" -> Icons.Outlined.Waves
            "bolt" -> Icons.Outlined.Bolt
            "display", "desktopcomputer" -> Icons.Outlined.Monitor
            "cpu" -> Icons.Outlined.Memory
            "network" -> Icons.Outlined.Wifi
            "function" -> Icons.Outlined.Functions
            "graph.3d" -> Icons.Outlined.AutoGraph
            "microphone" -> Icons.Outlined.Mic
            "lightbulb" -> Icons.Outlined.Lightbulb
            "tree" -> Icons.Outlined.Park

            "cylinder", "externaldrive", "server.rack" -> Icons.Outlined.Storage

            "scalemass" -> Icons.Outlined.Balance
            "text.book.closed", "book" -> Icons.Outlined.Book
            "globe.europe.africa" -> Icons.Outlined.Public
            "chart.bar" -> Icons.Outlined.BarChart
            "waveform.path.ecg" -> Icons.Outlined.MonitorHeart
            "building.columns" -> Icons.Outlined.AccountBalance
            "gearshape" -> Icons.Outlined.Settings

            else -> Icons.Outlined.Class
        }
    }
}