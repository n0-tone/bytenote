package com.notone.protodatastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.datastore by preferencesDataStore(name = "quicknotes_settings")

class UserPreferences(private val context: Context) {

    private val notesJsonKey = stringPreferencesKey(name = "notes_json")
    private val darkModeKey = booleanPreferencesKey(name = "dark_mode_enabled")

    fun notesFlow(): Flow<List<String>> {
        return context.datastore.data.map { preferences ->
            decodeNotes(preferences[notesJsonKey])
        }
    }

    suspend fun saveNotes(notes: List<String>) {
        context.datastore.edit { preferences ->
            preferences[notesJsonKey] = encodeNotes(notes)
        }
    }

    fun darkModeFlow(): Flow<Boolean> {
        return context.datastore.data.map { preferences ->
            preferences[darkModeKey] ?: false
        }
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        context.datastore.edit { preferences ->
            preferences[darkModeKey] = enabled
        }
    }

    private fun decodeNotes(rawJson: String?): List<String> {
        if (rawJson.isNullOrBlank()) return emptyList()

        return try {
            val jsonArray = JSONArray(rawJson)
            List(jsonArray.length()) { index ->
                jsonArray.optString(index, "")
            }.filter { it.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun encodeNotes(notes: List<String>): String {
        val jsonArray = JSONArray()
        notes.forEach { note ->
            jsonArray.put(note)
        }
        return jsonArray.toString()
    }
}