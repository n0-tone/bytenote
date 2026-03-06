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

data class QuickNote(
    val id: Long,
    val title: String,
    val content: String
)

class UserPreferences(private val context: Context) {

    private val notesJsonKey = stringPreferencesKey(name = "notes_json")
    private val darkModeKey = booleanPreferencesKey(name = "dark_mode_enabled")

    fun notesFlow(): Flow<List<QuickNote>> {
        return context.datastore.data.map { preferences ->
            decodeNotes(preferences[notesJsonKey])
        }
    }

    suspend fun saveNotes(notes: List<QuickNote>) {
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

    private fun decodeNotes(rawJson: String?): List<QuickNote> {
        if (rawJson.isNullOrBlank()) return emptyList()

        return try {
            val jsonArray = JSONArray(rawJson)
            List(jsonArray.length()) { index ->
                when (val item = jsonArray.opt(index)) {
                    is JSONObject -> {
                        val id = item.optLong("id", stableIdFrom(index, item.optString("content", "")))
                        val title = item.optString("title", "").ifBlank { "Sem titulo" }
                        val content = item.optString("content", "")
                        QuickNote(id = id, title = title, content = content)
                    }
                    is String -> {
                        // Backward compatibility with the previous string-only format.
                        val content = item.trim()
                        QuickNote(
                            id = stableIdFrom(index, content),
                            title = content.lineSequence().firstOrNull()?.take(30).orEmpty().ifBlank { "Sem titulo" },
                            content = content
                        )
                    }
                    else -> null
                }
            }.filterNotNull().filter { it.content.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun encodeNotes(notes: List<QuickNote>): String {
        val jsonArray = JSONArray()
        notes.forEach { note ->
            val jsonObject = JSONObject()
                .put("id", note.id)
                .put("title", note.title)
                .put("content", note.content)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    private fun stableIdFrom(index: Int, value: String): Long {
        return (31L * value.hashCode()) + index
    }
}