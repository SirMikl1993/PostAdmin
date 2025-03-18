package com.example.postadmin.data.models

data class Post(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val categoryId: String = "",
    val timestamp: Long = 0L
)