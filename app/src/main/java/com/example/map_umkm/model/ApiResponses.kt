package com.example.map_umkm.model

// Response untuk login
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User?
)

// Data user
data class User(
    val id: Int,
    val email: String,
    val role: String
)

// Response generic (misalnya untuk register)
data class GenericResponse(
    val success: Boolean,
    val message: String
)

// Item menu
data class MenuItem(
    val id: Int,
    val name: String,
    val description: String?,
    val price: Int,
    val image: String?
)

// Response menu
data class MenuResponse(
    val success: Boolean,
    val menu: List<MenuItem>?
)
