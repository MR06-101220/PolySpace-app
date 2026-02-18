package com.example.polyspace.data.parser

import com.example.polyspace.data.models.GradeClass
import com.example.polyspace.data.models.GradeEvaluation
import com.example.polyspace.data.models.GradeSemester
import com.example.polyspace.data.models.GradeYear
import com.example.polyspace.data.models.PolyGradeOverview
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class PolyGradeParser {

    private val dateFormats = listOf(
        "dd/MM/yyyy", "d/M/yyyy", "yyyy-MM-dd", "d MMMM yyyy"
    ).map { SimpleDateFormat(it, Locale.FRANCE) }

    fun parse(html: String, fetchedAt: Date = Date()): PolyGradeOverview {
        val document: Document = Jsoup.parse(html)

        // 1. Semesters Initialisation
        val semesters = initialAccumulators(document)

        // 2. Filling courses & grades
        parseCourses(document, semesters)
        parseGrades(document, semesters)

        return buildOverview(semesters, fetchedAt)
    }

    private fun initialAccumulators(document: Document): MutableMap<SemesterKey, SemesterAccumulator> {
        val accumulators = mutableMapOf<SemesterKey, SemesterAccumulator>()
        val headers = document.select("h2.semesterTitle")

        for (header in headers) {
            val year = header.attr("data-year").trim()
            val number = header.attr("data-num_semester").trim()
            if (year.isEmpty() || number == "0") continue

            val average = parseAverage(header.text())
            val key = SemesterKey(year, number)
            accumulators[key] =
                SemesterAccumulator(SemesterMeta(year, number, "Semestre $number", "", average))
        }
        return accumulators
    }

    // Courses
    private fun parseCourses(
        document: Document,
        semesters: MutableMap<SemesterKey, SemesterAccumulator>
    ) {
        val tables = document.select("table[id*=Courses]")

        for (table in tables) {
            val rawKey = findSemesterKeyForTable(table) ?: continue
            val key = findExistingKey(semesters, rawKey)

            val accumulator = semesters.getOrPut(key) {
                SemesterAccumulator(
                    SemesterMeta(
                        key.year,
                        key.number,
                        "Semestre ${key.number}",
                        "",
                        null
                    )
                )
            }

            val rows = table.select("tbody > tr")
            for (row in rows) {
                val cells = row.select("td")

                if (cells.size < 6) continue

                // Row 0 : UE
                val moduleNameRaw = cleanText(cells[0].text())
                    .replaceBefore("—", "") // Removing "bcb1, etc.."
                    .replace("—", "").trim()

                // Row 1 : Subject code
                val classCode = cleanText(cells[1].text())

                // Row 2 : Subject name
                val classTitle = cleanText(cells[2].text())

                // If there is no code or no title ==> pass
                if (classCode.isEmpty() || classTitle.isEmpty()) continue

                // Row 3 : Coefficient
                val coefficient = parseNumber(cleanText(cells[3].text()))

                // Row 4 : student average
                val studentAvg = parseNumber(cleanText(cells[5].text()))

                // Row 5 : promo average
                val promoAvg = if (cells.size > 6) parseNumber(cleanText(cells[6].text())) else null

                // Row 6 : rank
                val (rank, rankTotal) = if (cells.size > 7) parseRank(cleanText(cells[7].text())) else Pair(
                    null,
                    null
                )

                val classAcc = accumulator.classes.getOrPut(classCode) {
                    // Case if moduleNameRaw is empty
                    ClassAccumulator(
                        classCode,
                        classTitle,
                        moduleNameRaw.ifEmpty { "Enseignements" })
                }

                classAcc.coefficient = coefficient
                classAcc.studentAverage = studentAvg
                classAcc.promoAverage = promoAvg
                classAcc.rank = rank
                classAcc.rankTotal = rankTotal
            }
        }
    }

    // Grades
    private fun parseGrades(
        document: Document,
        semesters: MutableMap<SemesterKey, SemesterAccumulator>
    ) {
        val tables = document.select("table[id*=Tests]")

        for (table in tables) {
            val rawKey = findSemesterKeyForTable(table) ?: continue
            val key = findExistingKey(semesters, rawKey)
            val accumulator = semesters.getOrPut(key) {
                SemesterAccumulator(
                    SemesterMeta(
                        key.year,
                        key.number,
                        "Semestre ${key.number}",
                        "",
                        null
                    )
                )
            }

            val rows = table.select("tbody > tr")
            for (row in rows) {
                val cells = row.select("td")
                if (cells.size < 4) continue

                try {
                    val rawModuleInfo = cleanText(cells[0].text())

                    val moduleCode = rawModuleInfo.split(" ", "—").firstOrNull()?.trim() ?: ""

                    val assignment = cleanText(cells[1].text())
                    val gradeText = cleanText(cells[3].text()) // row 3 = grade
                    val grade = parseNumber(gradeText)

                    if (grade == null) continue

                    val evaluation = GradeEvaluation(
                        assignment = assignment.ifEmpty { "Note" },
                        date = parseDate(cleanText(cells[2].text())),
                        grade = grade,
                        classAverage = if (cells.size > 4) parseNumber(cleanText(cells[4].text())) else null,
                        rank = if (cells.size > 5) parseRank(cleanText(cells[5].text())).first else null,
                        totalPeople = null,
                        comments = ""
                    )

                    // Link to subject by searching wich subject has the same code
                    var targetClass = accumulator.classes[moduleCode]

                    // If not found, try to find by the first letter
                    if (targetClass == null && moduleCode.isNotEmpty()) {
                        targetClass = accumulator.classes.values.find { it.code == moduleCode }
                    }

                    // If still not found, try to find by the module name
                    if (targetClass == null) {
                        targetClass = accumulator.classes.values.find {
                            rawModuleInfo.contains(it.name, ignoreCase = true)
                        }
                    }

                    // If STILL not found, just create a subject for god sake
                    if (targetClass == null) {
                        targetClass = accumulator.classes.getOrPut(moduleCode) {
                            ClassAccumulator(moduleCode, rawModuleInfo, "Autre")
                        }
                    }
                    targetClass.evaluations.add(evaluation)

                } catch (e: Exception) {
                }
            }
        }
    }

    // Utils
    private fun findSemesterKeyForTable(table: Element): SemesterKey? {
        val id = table.id()
        val regexTableId = Pattern.compile("(Courses|Tests)(\\d)(\\d{4})")
        val matcher = regexTableId.matcher(id)

        if (matcher.find()) {
            val semNum = matcher.group(2).trim()
            val year = matcher.group(3).trim()
            return SemesterKey(year, semNum)
        }

        var node: Element? = table
        while (node != null) {
            val nodeId = node.id()
            if (nodeId.contains("Semester")) {
                val regexParent = Regex("Semester.*?_(\\d{4})_(\\d{1})")
                val match = regexParent.find(nodeId)
                if (match != null) {
                    val (year, number) = match.destructured
                    return SemesterKey(year.trim(), number.trim())
                }
            }
            node = node.parent()
        }
        return null
    }

    private fun findExistingKey(map: Map<SemesterKey, Any>, newKey: SemesterKey): SemesterKey {
        return map.keys.find { it.year == newKey.year && it.number == newKey.number } ?: newKey
    }

    private fun buildOverview(
        semesters: Map<SemesterKey, SemesterAccumulator>,
        fetchedAt: Date
    ): PolyGradeOverview {
        val yearsMap = mutableMapOf<String, MutableList<GradeSemester>>()
        for ((key, acc) in semesters) {
            val classes = acc.classes.values.map { it.build() }.sortedBy { it.name }
            if (classes.isNotEmpty() || acc.meta.average != null) {
                val semester =
                    GradeSemester(acc.meta.number, "", "", acc.meta.average, classes, listOf())
                yearsMap.getOrPut(key.year) { mutableListOf() }.add(semester)
            }
        }
        val gradeYears =
            yearsMap.map { (year, sems) -> GradeYear(year, sems.sortedBy { it.number }) }
                .sortedByDescending { it.year }
        return PolyGradeOverview(gradeYears, fetchedAt)
    }

    // Helpers
    private fun splitCodeAndNameSmart(text: String): Pair<String, String> {
        val parts = text.split(Regex("\\s+"), 2)
        if (parts.size == 2 && parts[0].any { it.isDigit() || it == '-' }) return Pair(
            parts[0],
            parts[1]
        )
        return Pair(text, text)
    }

    private fun parseAverage(text: String): Double? {
        val regex = Regex("moyenne.*?\\s*([\\d,]+)")
        val match = regex.find(text) ?: return null
        return match.groupValues[1].replace(",", ".").toDoubleOrNull()
    }

    private fun cleanText(text: String): String =
        text.replace("\u00a0", " ").replace(Regex("\\s+"), " ").trim()

    private fun parseNumber(text: String): Double? =
        if (text.isEmpty() || text == "-") null else text.replace(",", ".").toDoubleOrNull()

    private fun parseRank(text: String): Pair<Int?, Int?> {
        if (!text.contains("/")) return Pair(null, null)
        val parts = text.split("/")
        return if (parts.size == 2) Pair(
            parts[0].trim().toIntOrNull(),
            parts[1].trim().toIntOrNull()
        ) else Pair(null, null)
    }

    private fun parseDate(text: String): Date {
        val trimmed = cleanText(text)
        if (trimmed.isEmpty()) return Date(0)
        for (format in dateFormats) {
            try {
                return format.parse(trimmed) ?: continue
            } catch (e: Exception) {
                continue
            }
        }
        return Date(0)
    }
}

// Internal data classes (very important for the parser, but forgot why)
private data class SemesterKey(val year: String, val number: String)

private class SemesterAccumulator(val meta: SemesterMeta) {
    val classes = mutableMapOf<String, ClassAccumulator>()
}

private data class SemesterMeta(
    val year: String,
    val number: String,
    val kind: String,
    val status: String,
    val average: Double?
)

private class ClassAccumulator(var code: String, var name: String, var moduleName: String) {
    var coefficient: Double? = null
    var studentAverage: Double? = null
    var promoAverage: Double? = null
    var rank: Int? = null
    var rankTotal: Int? = null
    val evaluations = mutableListOf<GradeEvaluation>()
    fun build() = GradeClass(
        code,
        name,
        moduleName,
        coefficient,
        studentAverage,
        promoAverage,
        rank,
        rankTotal,
        evaluations.sortedByDescending { it.date })
}