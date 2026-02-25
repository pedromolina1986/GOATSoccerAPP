package com.example.goatsoccerapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.goatsoccerapp.navigation.AuthRoutes
import com.example.goatsoccerapp.ui.components.AuthTextField

@Composable
fun LoginScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "GOAT SOCCER MANAGER",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthTextField(email, { email = it }, "Email", modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(password, { password = it }, "Password", true, Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { navController.navigate(AuthRoutes.ResetPassword.route) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot password?")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* TODO: Login */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate(AuthRoutes.SignUp.route) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Don’t have an account? Sign up")
        }
    }
}