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
fun SignUpScreen(navController: NavController) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Create Account", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        AuthTextField(name, { name = it }, "Full Name", false, Modifier.fillMaxWidth())
        AuthTextField(email, { email = it }, "Email", false, Modifier.fillMaxWidth())
        AuthTextField(password, { password = it }, "Password", true, Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Account")
        }
    }
}