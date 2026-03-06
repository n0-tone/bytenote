package com.notone.protodatastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.datastore by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {

    private val textKey = stringPreferencesKey(name = "saved_text")
    private val textKey1 = stringPreferencesKey(name = "saved_text1")
    private val textKey2 = stringPreferencesKey(name = "saved_text2")
    private val switchKey = stringPreferencesKey(name = "switch_state")
    private val numberKey = stringPreferencesKey(name = "saved_number")

    fun getText(): Flow<String> {
        return context.datastore.data.map { preferences ->
            preferences[textKey] ?: ""
        }
    }

    suspend fun saveText(text: String) {
        context.datastore.edit { preferences ->
            preferences[textKey] = text
        }
    }

    fun getText1(): Flow<String> {
        return context.datastore.data.map { preferences ->
            preferences[textKey1] ?: ""
        }
    }

    suspend fun saveText1(text: String) {
        context.datastore.edit { preferences ->
            preferences[textKey1] = text
        }
    }

    fun getText2(): Flow<String> {
        return context.datastore.data.map { preferences ->
            preferences[textKey2] ?: ""
        }
    }

    suspend fun saveText2(text: String) {
        context.datastore.edit { preferences ->
            preferences[textKey2] = text
        }
    }

    fun getSwitchState(): Flow<Boolean> {
        return context.datastore.data.map { preferences ->
            preferences[switchKey]?.toBoolean() ?: false
        }
    }

    suspend fun saveSwitchState(state: Boolean) {
        context.datastore.edit { preferences ->
            preferences[switchKey] = state.toString()
        }
    }

    fun getNumber(): Flow<Int> {
        return context.datastore.data.map { preferences ->
            preferences[numberKey]?.toIntOrNull() ?: 0
        }
    }

    suspend fun saveNumber(number: Int) {
        context.datastore.edit { preferences ->
            preferences[numberKey] = number.toString()
        }
    }
}