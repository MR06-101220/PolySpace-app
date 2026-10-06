package com.polyspace.app.data.repository

import android.content.Context
import com.polyspace.app.data.local.CredentialStore
import com.polyspace.app.data.remote.PolyGradeNetwork
import com.polyspace.app.data.parser.PolyGradeParser
import com.polyspace.app.data.models.PolyGradeOverview
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GradesRepository(context: Context) {

    private val network = PolyGradeNetwork(context)
    private val parser = PolyGradeParser()
    private val credentialStore = CredentialStore(context)
    private val gson = Gson()
    private val sharedPrefs = context.getSharedPreferences("grades_prefs", Context.MODE_PRIVATE)

    // Credentials
    fun saveCredentials(user: String, pass: String) {
        credentialStore.saveCredentials(user, pass)
    }

    fun getSavedCredentials(): Pair<String?, String?> {
        val creds = credentialStore.getCredentials()
        return if (creds != null) Pair(creds.first, creds.second) else Pair(null, null)
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