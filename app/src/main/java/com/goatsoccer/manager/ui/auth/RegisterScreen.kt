package com.goatsoccer.manager.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goatsoccer.manager.ui.theme.GreenPrimary
import com.goatsoccer.manager.util.Resource
import com.goatsoccer.manager.util.appViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = appViewModel()
) {
    var name            by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole    by remember { mutableStateOf("fan") }
    var emailFocused    by remember { mutableStateOf(false) }

    val registerResult by viewModel.registerResult.observeAsState()
    val inviteLookup   by viewModel.inviteLookup.observeAsState()
    val isInvited      = inviteLookup?.found == true

    LaunchedEffect(registerResult) {
        if (registerResult is Resource.Success) onRegisterSuccess()
    }

    // Auto-fill name from invite
    LaunchedEffect(inviteLookup) {
        if (isInvited && inviteLookup?.name?.isNotBlank() == true) {
            name = inviteLookup!!.name
        }
    }

    val isLoading = registerResult is Resource.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            color = GreenPrimary
        )
        Spacer(Modifier.height(24.dp))

        // Invite banner
        if (isInvited) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Your details have been pre-filled for ${inviteLookup?.teamName}",
                    color = Color.White,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // Email — triggers invite lookup on focus lost
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focus ->
                    if (emailFocused && !focus.isFocused && email.isNotBlank()) {
                        viewModel.lookupInvite(email.trim())
                    }
                    emailFocused = focus.isFocused
                }
        )
        Spacer(Modifier.height(12.dp))

        // Name — locked when invited
        OutlinedTextField(
            value = name,
            onValueChange = { if (!isInvited) name = it },
            label = { Text("Full Name") },
            singleLine = true,
            enabled = !isInvited,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // Role selector — hidden when invited
        if (!isInvited) {
            Text(
                text = "Role",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("fan" to "Fan", "player" to "Player", "coach" to "Coach").forEach { (key, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        RadioButton(
                            selected = selectedRole == key,
                            onClick = { selectedRole = key }
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        } else {
            OutlinedTextField(
                value = "Player (set by invite)",
                onValueChange = {},
                label = { Text("Role") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        if (registerResult is Resource.Error) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = (registerResult as Resource.Error).message ?: "Registration failed",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val finalRole = if (isInvited) "player" else selectedRole
                viewModel.register(
                    name.trim(), email.trim(),
                    password.trim(), confirmPassword.trim(), finalRole
                )
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Register", fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack) {
            Text("Already have an account? Login")
        }
    }
}
