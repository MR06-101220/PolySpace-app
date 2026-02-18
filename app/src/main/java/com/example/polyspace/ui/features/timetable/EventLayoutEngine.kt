package com.example.polyspace.ui.features.timetable

import com.example.polyspace.data.models.CourseEvent
import com.example.polyspace.data.models.PositionedEvent

/**
 * Moteur de mise en page des événements.
 * Gère l'algorithme "Tetris" pour empiler les cours qui se chevauchent.
 */
object EventLayoutEngine {

    fun calculatePositions(events: List<CourseEvent>): List<PositionedEvent> {
        if (events.isEmpty()) return emptyList()

        val positioned = mutableListOf<PositionedEvent>()
        val clusters = buildClusters(events)

        for (cluster in clusters) {
            val colEnds = mutableListOf<String>()
            val clusterResult = mutableListOf<PositionedEvent>()

            for (event in cluster) {
                var placed = false
                for (i in colEnds.indices) {
                    if (colEnds[i] <= event.start) {
                        colEnds[i] = event.end
                        clusterResult.add(PositionedEvent(event, i, 0))
                        placed = true
                        break
                    }
                }
                if (!placed) {
                    colEnds.add(event.end)
                    clusterResult.add(PositionedEvent(event, colEnds.lastIndex, 0))
                }
            }

            val totalCols = colEnds.size
            clusterResult.forEach { positioned.add(it.copy(totalColumns = totalCols)) }
        }
        return positioned
    }

    private fun buildClusters(events: List<CourseEvent>): List<List<CourseEvent>> {
        val clusters = mutableListOf<MutableList<CourseEvent>>()
        for (event in events) {
            val lastCluster = clusters.lastOrNull()
            if (lastCluster != null) {
                val clusterEnd = lastCluster.maxOf { it.end }
                if (event.start < clusterEnd) {
                    lastCluster.add(event)
                    continue
                }
            }
            clusters.add(mutableListOf(event))
        }
        return clusters
    }
}