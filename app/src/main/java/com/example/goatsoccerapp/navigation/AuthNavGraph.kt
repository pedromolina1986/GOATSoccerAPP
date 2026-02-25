package com.example.goatsoccerapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.goatsoccerapp.ui.screens.LoginScreen
import com.example.goatsoccerapp.ui.screens.SignUpScreen
import com.example.goatsoccerapp.ui.screens.ResetPasswordScreen

@Composable
fun AuthNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AuthRoutes.Login.route
    ) {
        composable(AuthRoutes.Login.route) {
            LoginScreen(navController)
        }
        composable(AuthRoutes.SignUp.route) {
            SignUpScreen(navController)
        }
        composable(AuthRoutes.ResetPassword.route) {
            ResetPasswordScreen(navController)
        }
    }
}