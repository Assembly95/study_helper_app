package com.example.myapplication

data class Question(
    val id: Long = System.currentTimeMillis(),
    val subject: String,
    val chapter: String,
    val question: String,
    val answer: String,
    val options: List<String>? = null,
    val difficulty: Int = 1,
    val isUserCreated: Boolean = true,
    var lastReviewDate: Long? = null,
    var correctCount: Int = 0,
    var incorrectCount: Int = 0
)