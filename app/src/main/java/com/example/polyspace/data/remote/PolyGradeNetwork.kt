package com.example.polyspace.data.remote

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class PolyGradeNetwork(private val context: Context) {

    private val cookieJar = SessionCookieJar()
    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BASE_URL = "https://polytech-saclay.oasis.aouka.org"
        private const val AJAX_PATH = "$BASE_URL/prod/bo/core/Router/Ajax/ajax.php"
        private val COMMON_HEADERS = mapOf(
            "User-Agent" to "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
            "X-Requested-With" to "XMLHttpRequest",
            "Origin" to BASE_URL,
            "Referer" to "$BASE_URL/"
        )
    }

    suspend fun login(username: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        val loginUrl = AJAX_PATH.toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("targetProject", "oasis_polytech_paris")
            .addQueryParameter("route", "BO\\Connection\\User::login")
            .build()

        val formBody = FormBody.Builder()
            .add("login", username)
            .add("password", pass)
            .add("url", "")
            .build()

        val request = Request.Builder().url(loginUrl).post(formBody)
            .apply { COMMON_HEADERS.forEach { (k, v) -> header(k, v) } }.build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (body.contains("\"success\":false")) return@withContext false
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            return@withContext false
        }
    }

    suspend fun fetchGradesHtml(): String? = withContext(Dispatchers.IO) {
        // 1. Main page
        val mainUrl = AJAX_PATH.toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("targetProject", "oasis_polytech_paris")
            .addQueryParameter("route", "BO\\Layout\\MainContent::load")
            .addQueryParameter("codepage", "MYMARKS")
            .build()

        var htmlContent = ""
        try {
            val request = Request.Builder().url(mainUrl).get()
                .apply { COMMON_HEADERS.forEach { (k, v) -> header(k, v) } }.build()
            client.newCall(request).execute().use { htmlContent = it.body?.string() ?: "" }
        } catch (e: Exception) {
            return@withContext null
        }

        if (htmlContent.contains("Vous ne pouvez pas accéder") && !htmlContent.contains("load(sReloadSemesterRoute")) {
            throw MissingPhotoException()
        }

        val fullHtml = StringBuilder()
        fullHtml.append(htmlContent)

        // 2. Semesters detection
        val regex =
            Pattern.compile("(?s)student:\\s*'(\\d+)',\\s*year:\\s*(\\d+),\\s*semester_in_year:\\s*(\\d+)")
        val matcher = regex.matcher(htmlContent)
        val processedKeys = mutableSetOf<String>()

        while (matcher.find()) {
            val studentId = matcher.group(1)
            val year = matcher.group(2)
            val semester = matcher.group(3)
            val key = "$year-$semester"

            if (!processedKeys.contains(key)) {
                processedKeys.add(key)

                // 3. Downloading tab
                val coursesContent = fetchTabContent(studentId!!, year!!, semester!!, "Courses")
                fullHtml.append("\n<hr>\n").append(coursesContent)

                val testsContent = fetchTabContent(studentId, year, semester, "Tests")
                fullHtml.append("\n<hr>\n").append(testsContent)
            }
        }

        return@withContext fullHtml.toString()
    }

    private fun fetchTabContent(
        studentId: String,
        year: String,
        semester: String,
        tabName: String
    ): String {
        val url =
            "$BASE_URL/prod/bo/core/Router/Ajax/ajax.php?targetProject=oasis_polytech_paris&route=Oasis\\Common\\Model\\Cursus\\StudentCursus\\StudentCursus::reload_semester"
        val formBody = FormBody.Builder()
            .add("student", studentId)
            .add("year", year)
            .add("semester_in_year", semester)
            .add("tab", tabName)
            .build()

        val request = Request.Builder().url(url).post(formBody)
            .apply { COMMON_HEADERS.forEach { (k, v) -> header(k, v) } }.build()

        return try {
            client.newCall(request).execute().use { it.body?.string() ?: "" }
        } catch (e: Exception) {
            ""
        }
    }

    fun logout() {
        cookieJar.clear()
    }
}