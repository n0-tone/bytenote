package com.notone.protodatastore.classes

import android.content.Context
import com.notone.protodatastore.core.PreferencesService
import com.notone.protodatastore.enums.PreferencesKeysEnum
import kotlinx.coroutines.flow.Flow

class UserPreferencesService(private val context: Context) {

    private val _service = PreferencesService(context)

    fun getDarkMode() : Flow<Boolean> {
        val darkMode = _service.getValue<Boolean>(
            PreferencesKeysEnum.DarkMode
        )
        return darkMode
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        _service.saveValue<Boolean>(
            enabled,
            PreferencesKeysEnum.DarkMode
        )
    }
}