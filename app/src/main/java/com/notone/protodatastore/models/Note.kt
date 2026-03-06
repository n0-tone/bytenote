package com.notone.protodatastore.models

data class Note(
    val id : Long,
    val title: String,
    val content: String,
    val imageUri: String? = null
)