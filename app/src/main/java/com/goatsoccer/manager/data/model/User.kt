package com.goatsoccer.manager.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("role") val role: String = "fan", // coach, player, fan
    @SerializedName("token") val token: String = "",
    @SerializedName("teamId") val teamId: String = "",
    @SerializedName("createdAt") val createdAt: String = ""
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: User
)

data class ErrorBody(
    @SerializedName("message") val message: String = ""
)
