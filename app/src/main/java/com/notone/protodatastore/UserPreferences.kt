package com.notone.protodatastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.datastore by preferencesDataStore(name = "quicknotes_settings")

data class Note(
    val title: String,
    val content: String,
    val imageUri: String? = null
)

class UserPreferences(private val context: Context) {

    private val notesJsonKey = stringPreferencesKey(name = "notes_json_v3")
    private val darkModeKey = booleanPreferencesKey(name = "dark_mode_enabled")

    fun notesFlow(): Flow<List<Note>> {
        return context.datastore.data.map { preferences ->
            decodeNotes(preferences[notesJsonKey])
        }
    }

    suspend fun saveNotes(notes: List<Note>) {
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

    private fun decodeNotes(rawJson: String?): List<Note> {
        if (rawJson.isNullOrBlank()) return emptyList()

        return try {
            val jsonArray = JSONArray(rawJson)
            val list = mutableListOf<Note>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Note(
                        title = obj.optString("title", ""),
                        content = obj.optString("content", ""),
                        imageUri = if (obj.has("imageUri") && !obj.isNull("imageUri")) obj.getString("imageUri") else null
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun encodeNotes(notes: List<Note>): String {
        val jsonArray = JSONArray()
        notes.forEach { note ->
            val obj = JSONObject()
            obj.put("title", note.title)
            obj.put("content", note.content)
            if (note.imageUri != null) {
                obj.put("imageUri", note.imageUri)
            } else {
                obj.put("imageUri", JSONObject.NULL)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}
