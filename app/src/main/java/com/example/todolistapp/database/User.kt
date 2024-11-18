package com.example.todolistapp.database

data class User(
    var id: Int = 0,
    var username: String,
    var email: String,
    var password: String
)