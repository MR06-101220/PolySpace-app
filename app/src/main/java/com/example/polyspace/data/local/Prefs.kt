package com.example.polyspace.data.local

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREFS_NAME = "polyspace_prefs"
    private const val KEY_USER_TYPE = "user_type"
    private const val KEY_RESOURCE_ID = "resource_id"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_KNOWN_SUBJECTS = "known_subjects"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isSetupDone(): Boolean {
        return prefs.contains(KEY_USER_TYPE)
    }

    fun savePromo(promoName: String) {
        prefs.edit().apply {
            putString(KEY_USER_TYPE, "PROMO")
            putString(KEY_RESOURCE_ID, promoName)
            putString(KEY_DISPLAY_NAME, promoName)
            apply()
        }
    }

    fun saveStudent(id: String, name: String) {
        prefs.edit().apply {
            putString(KEY_USER_TYPE, "STUDENT")
            putString(KEY_RESOURCE_ID, id)
            putString(KEY_DISPLAY_NAME, name)
            apply()
        }
    }

    fun getUserType(): String = prefs.getString(KEY_USER_TYPE, "PROMO") ?: "PROMO"
    fun getResourceId(): String = prefs.getString(KEY_RESOURCE_ID, "ET3") ?: "ET3"
    fun getDisplayName(): String = prefs.getString(KEY_DISPLAY_NAME, "Mon EDT") ?: "Mon EDT"

    fun getKnownSubjects(): Set<String> {
        return prefs.getStringSet(KEY_KNOWN_SUBJECTS, emptySet()) ?: emptySet()
    }

    fun addKnownSubjects(newSubjects: List<String>) {
        val current = getKnownSubjects().toMutableSet()
        current.addAll(newSubjects)
        prefs.edit().putStringSet(KEY_KNOWN_SUBJECTS, current).apply()
    }

    fun clearSubjects() {
        prefs.edit().remove(KEY_KNOWN_SUBJECTS).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}