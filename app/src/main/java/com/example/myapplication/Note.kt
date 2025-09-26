package com.example.myapplication

data class Note(
    val id: Long = System.currentTimeMillis(),
    var title: String,
    var content: String,
    val createdDate: Long = System.currentTimeMillis(),
    var modifiedDate: Long = System.currentTimeMillis()
)