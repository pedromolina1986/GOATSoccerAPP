package com.goatsoccer.manager.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.goatsoccer.manager.data.local.SessionManager
import com.goatsoccer.manager.ui.MainActivity
import com.goatsoccer.manager.ui.theme.GoatSoccerTheme
import com.goatsoccer.manager.util.ViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Skip login if already authenticated
        if (SessionManager(this).isLoggedIn()) {
            navigateToMain()
            return
        }

        setContent {
            GoatSoccerTheme {
                val navController = rememberNavController()
                val factory = ViewModelFactory(SessionManager(this))

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess  = { navigateToMain() },
                            onNavigateToRegister = { navController.navigate("register") },
                            viewModel = viewModel(factory = factory)
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = { navigateToMain() },
                            onNavigateBack    = { navController.popBackStack() },
                            viewModel = viewModel(factory = factory)
                        )
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
