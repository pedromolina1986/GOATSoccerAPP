package com.example.goatsoccerapp.navigation

sealed class AuthRoutes(val route: String) {
    object Login : AuthRoutes("login")
    object SignUp : AuthRoutes("signup")
    object ResetPassword : AuthRoutes("reset_password")
}