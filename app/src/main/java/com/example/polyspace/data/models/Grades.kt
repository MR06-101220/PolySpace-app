package com.example.polyspace.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

// See everything
@Parcelize
data class PolyGradeOverview(
    val years: List<GradeYear>,
    val fetchedAt: Date
) : Parcelable

// Get year
@Parcelize
data class GradeYear(
    val year: String,
    val semesters: List<GradeSemester>
) : Parcelable {
    val id: String get() = year
}

// Get semester
@Parcelize
data class GradeSemester(
    val number: String, // ex: "5"
    val kind: String,   // ex: "Automne"
    val status: String, // ex: "Validé"
    val average: Double?, // Semester average
    val classes: List<GradeClass>, // Subject
    val modules: List<GradeModule> // UEs
) : Parcelable {
    val id: String get() = number

    // Helper for getting the number as an int (usefull for sort)
    val intNumber: Int get() = number.toIntOrNull() ?: 0
}

// Subject / Course
@Parcelize
data class GradeClass(
    val code: String,
    val name: String,
    val moduleName: String,
    val coefficient: Double?,

    val studentAverage: Double?,
    val promoAverage: Double?,

    val classRank: Int?,
    val classRankTotal: Int?,
    val evaluations: List<GradeEvaluation>
) : Parcelable {

    val id: String get() = if (code.isEmpty()) name else code

    // In case something went wrong
    val computedAverage: Double?
        get() {
            val grades = evaluations.mapNotNull { it.grade }
            if (grades.isEmpty()) return null
            return grades.sum() / grades.size
        }
}

// UE
@Parcelize
data class GradeModule(
    val code: String,
    val name: String,
    val classCodes: List<String>
) : Parcelable {
    val id: String get() = if (code.isEmpty()) name else code
}

// Evaluation/Assignment
@Parcelize
data class GradeEvaluation(
    val assignment: String,
    val date: Date,
    val grade: Double?,
    val classAverage: Double?,
    val rank: Int?,
    val totalPeople: Int?,
    val comments: String
) : Parcelable {
    // Unique ID based on date and assignment
    val id: String get() = "$assignment-${date.time}"
}