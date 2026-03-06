package com.notone.protodatastore.classes

import android.content.Context
import com.notone.protodatastore.core.PreferencesService
import com.notone.protodatastore.enums.PreferencesKeysEnum
import com.notone.protodatastore.models.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class NotesService(private val context: Context) {

    private val _service = PreferencesService(context)

    suspend fun saveNotes(notes: List<Note>) {
        val encodedNotes = encodeNotes(notes)
        _service.saveValue<String>(
            encodedNotes,
            PreferencesKeysEnum.Notes
        )
    }

    fun getNotes() : Flow<List<Note>> {
        val encodedNote = _service.getValue<String>(
            PreferencesKeysEnum.Notes
        )

        return encodedNote.map {
            decodeNotes(it)
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
                        id = obj.getLong("id"),
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
            obj.put("id", note.id)
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