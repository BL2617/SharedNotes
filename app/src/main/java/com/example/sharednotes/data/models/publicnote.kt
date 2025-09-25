package com.example.sharednotes.data.models

data class PublicNote(
    val id: Long,
    val title: String,
    val content: String,
    val author: String,
    val createdAt: String
)