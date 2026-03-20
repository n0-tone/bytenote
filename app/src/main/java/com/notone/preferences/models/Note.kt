package com.notone.preferences.models

data class Note(
    val id : Long,
    val title: String,
    val content: String,
    val imageUri: String? = null
)