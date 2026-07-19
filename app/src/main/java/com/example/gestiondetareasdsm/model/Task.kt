package com.example.gestiondetareasdsm.model

data class Task(
    val id: Int,
    var title: String,
    var completed: Boolean = false,
    var active: Boolean = false
)