package com.example.polyspace.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CredentialStore(context: Context) {

    // Getting/creating the key
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Open encrypted shared preferences
    private val sharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "secure_grades_prefs", // Nom du fichier
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // If any issue occur, file is recreated
        context.getSharedPreferences("secure_grades_prefs", Context.MODE_PRIVATE).edit().clear()
            .apply()
        EncryptedSharedPreferences.create(
            context,
            "secure_grades_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Save (username, password)
    fun saveCredentials(username: String, pass: String) {
        sharedPreferences.edit()
            .putString("username", username)
            .putString("password", pass)
            .apply()
    }

    // Getting username and password
    fun getCredentials(): Pair<String, String>? {
        val user = sharedPreferences.getString("username", null)
        val pass = sharedPreferences.getString("password", null)

        return if (user != null && pass != null) {
            Pair(user, pass)
        } else {
            null
        }
    }

    // Clearing everything in the file
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}