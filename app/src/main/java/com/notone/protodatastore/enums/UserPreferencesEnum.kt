package com.notone.protodatastore.enums
import kotlin.reflect.KClass
enum class PreferencesKeysEnum(val value : String, val type : KClass<*>) {
    Notes("notes_json",String::class),
    DarkMode("dark_mode_enabled",Boolean::class)
}

