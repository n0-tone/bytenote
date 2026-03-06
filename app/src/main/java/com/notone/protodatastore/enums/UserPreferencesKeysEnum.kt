package com.notone.protodatastore.enums
import kotlin.reflect.KClass
enum class UserPreferencesKeysEnum(val value : String,val type : KClass<*>) {
    Text_Input_1 ("saved_text_1",String::class),
    Text_Input_2("saved_text_2",String::class),
    Switch ("saved_switch",Boolean::class),
    Number ("saved_number",Int::class)
}