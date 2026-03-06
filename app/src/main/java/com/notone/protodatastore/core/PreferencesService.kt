package com.notone.protodatastore.core
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.notone.protodatastore.enums.PreferencesKeysEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.datastore by preferencesDataStore(name = "quicknotes_settings")

class PreferencesService(private val context: Context)
{
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getValue(preferencesKeysEnum: PreferencesKeysEnum): Flow<T> {

        return when (preferencesKeysEnum.type) {
            String::class -> {
                getString(preferencesKeysEnum) as Flow<T>
            }

            Boolean::class -> {
                getBoolean(preferencesKeysEnum) as Flow<T>
            }

            Int::class -> {
                getInteger(preferencesKeysEnum) as Flow<T>
            }

            else -> {
                throw IllegalArgumentException("Type not supported")
            }
        }
    }

    suspend fun <T : Any> saveValue(value: T, userPreferenceEnum: PreferencesKeysEnum) {
        if (value::class == userPreferenceEnum.type :: class) {
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
        preferencesKeysEnum: PreferencesKeysEnum
    ) {
        val key = stringPreferencesKey(preferencesKeysEnum.value)
        context.datastore.edit { preferences ->
            preferences[key] = value
        }
    }

    private suspend fun saveBoolean(
        value: Boolean,
        preferencesKeysEnum: PreferencesKeysEnum
    ) {
        val key = booleanPreferencesKey(preferencesKeysEnum.value)
        context.datastore.edit { preferences ->
            preferences[key] = value
        }
    }

    private suspend fun saveInteger(value: Int, preferencesKeysEnum: PreferencesKeysEnum) {
        val key = intPreferencesKey(preferencesKeysEnum.value)
        context.datastore.edit { preferences ->
            preferences[key] = value
        }
    }

    private fun getString(preferencesKeysEnum: PreferencesKeysEnum): Flow<String> {
        val key = stringPreferencesKey(preferencesKeysEnum.value)
        return context.datastore.data.map { preferences ->
            preferences[key] ?: ""
        }
    }

    private fun getBoolean(preferencesKeysEnum: PreferencesKeysEnum): Flow<Boolean> {
        val key = booleanPreferencesKey(preferencesKeysEnum.value)
        return context.datastore.data.map { preferences ->
            preferences[key] ?: false
        }
    }

    private fun getInteger(preferencesKeysEnum: PreferencesKeysEnum): Flow<Int> {
        val key = intPreferencesKey(preferencesKeysEnum.value)
        return context.datastore.data.map { preferences ->
            preferences[key] ?: 0
        }
    }

}
