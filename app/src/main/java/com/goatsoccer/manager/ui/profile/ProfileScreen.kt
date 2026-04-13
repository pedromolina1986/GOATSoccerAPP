package com.goatsoccer.manager.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goatsoccer.manager.data.model.User
import com.goatsoccer.manager.ui.theme.GreenDark
import com.goatsoccer.manager.ui.theme.GreenPrimary
import com.goatsoccer.manager.util.Resource
import com.goatsoccer.manager.util.appViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = appViewModel()
) {
    val profileState    by viewModel.profile.observeAsState()
    val saveSuccess     by viewModel.saveSuccess.observeAsState()
    val deactivateResult by viewModel.deactivateResult.observeAsState()
    val snackbarHost    = remember { SnackbarHostState() }

    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showLogoutDialog     by remember { mutableStateOf(false) }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess == true) snackbarHost.showSnackbar("Profile updated!")
    }
    LaunchedEffect(deactivateResult) {
        when (deactivateResult) {
            is Resource.Success -> onLogout()
            is Resource.Error   -> snackbarHost.showSnackbar(
                (deactivateResult as Resource.Error).message ?: "Error deactivating account")
            else -> {}
        }
    }

    val isDeactivating = deactivateResult is Resource.Loading

    Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val state = profileState) {
                is Resource.Loading, null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text  = state.message ?: "Failed to load profile",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadProfile() }) { Text("Retry") }
                    }
                }
                is Resource.Success -> {
                    ProfileContent(
                        user           = state.data!!,
                        role           = viewModel.userRole,
                        isDeactivating = isDeactivating,
                        onSave         = { name -> viewModel.saveProfile(name) },
                        onLogout       = { showLogoutDialog = true },
                        onDeactivate   = { showDeactivateDialog = true }
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text  = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = { Text("Deactivate Account") },
            text  = {
                Text(
                    "Your account will be deactivated. All your history (roasts, matches) " +
                    "will be preserved.\n\nYou can create a new account with the same email address later."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeactivateDialog = false
                    viewModel.deactivateAccount()
                }) {
                    Text("Deactivate", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileContent(
    user: User,
    role: String,
    isDeactivating: Boolean,
    onSave: (String) -> Unit,
    onLogout: () -> Unit,
    onDeactivate: () -> Unit
) {
    var name by remember(user.name) { mutableStateOf(user.name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Avatar
        Surface(
            shape    = CircleShape,
            color    = GreenPrimary,
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text       = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize   = 36.sp,
                    color      = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Role badge
        Surface(
            shape = MaterialTheme.shapes.small,
            color = GreenPrimary.copy(alpha = 0.15f)
        ) {
            Text(
                text     = role.replaceFirstChar { it.uppercase() },
                color    = GreenPrimary,
                style    = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Divider()

        // Section header
        Text(
            text     = "Account Information",
            style    = MaterialTheme.typography.titleSmall,
            color    = GreenDark,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )

        // Name (editable)
        OutlinedTextField(
            value         = name,
            onValueChange = { name = it },
            label         = { Text("Display Name") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        // Email (read-only)
        OutlinedTextField(
            value         = user.email,
            onValueChange = {},
            label         = { Text("Email") },
            enabled       = false,
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        // Role (read-only)
        OutlinedTextField(
            value         = role.replaceFirstChar { it.uppercase() },
            onValueChange = {},
            label         = { Text("Role") },
            enabled       = false,
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        // Team (players only)
        if (role == "player" && user.teamId.isNotBlank()) {
            OutlinedTextField(
                value         = user.teamId,
                onValueChange = {},
                label         = { Text("Team ID") },
                enabled       = false,
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
        }

        // Member since
        if (user.createdAt.isNotBlank()) {
            OutlinedTextField(
                value         = user.createdAt.take(10),
                onValueChange = {},
                label         = { Text("Member since") },
                enabled       = false,
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick  = { onSave(name.trim()) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Save Changes", fontSize = 16.sp)
        }

        OutlinedButton(
            onClick  = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Logout", fontSize = 16.sp)
        }

        Spacer(Modifier.height(8.dp))

        Divider()

        Text(
            text     = "Danger Zone",
            style    = MaterialTheme.typography.titleSmall,
            color    = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text  = "Deactivating your account will sign you out and block future logins. " +
                    "Your history is preserved and you can register again with this email.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick  = onDeactivate,
            enabled  = !isDeactivating,
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isDeactivating) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.error
                )
            } else {
                Text("Deactivate Account", fontSize = 16.sp)
            }
        }
    }
}
