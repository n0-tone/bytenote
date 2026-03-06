package com.notone.protodatastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.notone.protodatastore.enums.UserPreferencesKeysEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.datastore by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getValue(userPreferencesKeysEnum: UserPreferencesKeysEnum): Flow<T> {

        return when (userPreferencesKeysEnum.type) {
            String::class -> {
                getString(userPreferencesKeysEnum) as Flow<T>
            }

            Boolean::class -> {
                getBoolean(userPreferencesKeysEnum) as Flow<T>
            }

            Int::class -> {
                getInteger(userPreferencesKeysEnum) as Flow<T>
            }

            else -> {
                throw IllegalArgumentException("Type not supported")
            }
        }
    }

    suspend fun <T : Any> saveValue(value: T, userPreferenceEnum: UserPreferencesKeysEnum) {
        if (value::class == userPreferenceEnum.type) {
            throw IllegalArgumentException("Type not supported")
        }
        when (userPreferenceEnum.type) {
            String::class -> {
                saveString(value as String, userPreferenceEnum)
            }

            Boolean::class -> {
                saveBoolean(value as Boolean, userPreferenceEnum)
            }

            Int::class -> {
                saveInteger(value as Int, userPreferenceEnum)
            }
        }

    }

    private suspend fun saveString(
        value: String,
        userPreferencesKeysEnum: UserPreferencesKeysEnum
    ) {
        val key = stringPreferencesKey(userPreferencesKeysEnum.value)
        context.datastore.edit { preferences ->
            preferences[key] = value
        }
    }

    private suspend fun saveBoolean(
        value: Boolean,
        userPreferencesKeysEnum: UserPreferencesKeysEnum
    ) {
        val key = booleanPreferencesKey(userPreferencesKeysEnum.value)
        context.datastore.edit { preferences ->
            preferences[key] = value
        }
    }

    private suspend fun saveInteger(value: Int, userPreferencesKeysEnum: UserPreferencesKeysEnum) {
        val key = intPreferencesKey(userPreferencesKeysEnum.value)
        context.datastore.edit { preferences ->
            preferences[key] = value
        }
    }

    private fun getString(userPreferencesKeysEnum: UserPreferencesKeysEnum): Flow<String> {
        val key = stringPreferencesKey(userPreferencesKeysEnum.value)
        return context.datastore.data.map { preferences ->
            preferences[key] ?: ""
        }
    }

    private fun getBoolean(userPreferencesKeysEnum: UserPreferencesKeysEnum): Flow<Boolean> {
        val key = booleanPreferencesKey(userPreferencesKeysEnum.value)
        return context.datastore.data.map { preferences ->
            preferences[key] ?: false
        }
    }

    private fun getInteger(userPreferencesKeysEnum: UserPreferencesKeysEnum): Flow<Int> {
        val key = intPreferencesKey(userPreferencesKeysEnum.value)
        return context.datastore.data.map { preferences ->
            preferences[key] ?: 0
        }
    }
}
