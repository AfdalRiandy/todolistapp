package com.example.todolistapp.database

data class Todo(
    var id: Int = 0,
    var userId: Int,
    var task: String,
    var dueDate: Long? = null,
    var isCompleted: Boolean = false

)