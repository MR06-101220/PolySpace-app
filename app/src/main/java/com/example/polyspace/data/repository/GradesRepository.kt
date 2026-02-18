package com.example.polyspace.data.repository

import android.content.Context
import com.example.polyspace.data.remote.PolyGradeNetwork
import com.example.polyspace.data.parser.PolyGradeParser
import com.example.polyspace.data.models.PolyGradeOverview
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GradesRepository(context: Context) {

    private val network = PolyGradeNetwork(context)
    private val parser = PolyGradeParser()
    private val gson = Gson()
    private val sharedPrefs = context.getSharedPreferences("grades_prefs", Context.MODE_PRIVATE)

    // Credentials
    fun saveCredentials(user: String, pass: String) {
        sharedPrefs.edit().putString("saved_user", user).putString("saved_pass", pass).apply()
    }

    fun getSavedCredentials(): Pair<String?, String?> {
        return Pair(
            sharedPrefs.getString("saved_user", null),
            sharedPrefs.getString("saved_pass", null)
        )
    }

    // Login
    suspend fun login(user: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext network.login(user, pass)
    }

    // Fetch + Parse
    // Return parsed object or null if error
    suspend fun fetchGrades(): PolyGradeOverview? = withContext(Dispatchers.IO) {
        val html = network.fetchGradesHtml() ?: return@withContext null
        return@withContext parser.parse(html)
    }

    // Cache
    fun saveToCache(overview: PolyGradeOverview) {
        try {
            val json = gson.toJson(overview)
            sharedPrefs.edit().putString("cached_grades_json", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadFromCache(): PolyGradeOverview? {
        return try {
            val json = sharedPrefs.getString("cached_grades_json", null)
            if (json != null) {
                gson.fromJson(json, PolyGradeOverview::class.java)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun clearCache() {
        sharedPrefs.edit().remove("cached_grades_json").apply()
    }
}