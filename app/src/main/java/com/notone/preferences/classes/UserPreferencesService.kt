package com.notone.preferences.classes

import android.content.Context
import com.notone.preferences.core.PreferencesService
import com.notone.preferences.enums.PreferencesKeysEnum
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