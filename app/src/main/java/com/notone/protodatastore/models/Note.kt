package com.notone.protodatastore.models

data class Note(
    val title: String,
    val content: String,
    val imageUri: String? = null
)