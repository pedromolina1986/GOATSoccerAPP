package com.goatsoccer.manager.ui.roasts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goatsoccer.manager.data.local.SessionManager
import com.goatsoccer.manager.data.model.Roast
import com.goatsoccer.manager.ui.theme.GreenPrimary
import com.goatsoccer.manager.util.Resource
import com.goatsoccer.manager.util.appViewModel

@Composable
fun RoastsScreen(viewModel: RoastsViewModel = appViewModel()) {
    val context       = LocalContext.current
    val currentUserId = remember { SessionManager(context).getUserId() ?: "" }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.silentRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val roastsState by viewModel.roasts.observeAsState()
    val postResult  by viewModel.postResult.observeAsState()

    var content      by remember { mutableStateOf("") }
    var roastToDelete by remember { mutableStateOf<Roast?>(null) }
    val snackbarHost  = remember { SnackbarHostState() }

    LaunchedEffect(postResult) {
        when (postResult) {
            is Resource.Success -> { content = ""; snackbarHost.showSnackbar("Roast posted!") }
            is Resource.Error   -> snackbarHost.showSnackbar((postResult as Resource.Error).message ?: "Error")
            else -> {}
        }
    }

    // No inner Scaffold — Box overlay avoids nested-Scaffold crashes
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // Compose input area
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value         = content,
                        onValueChange = { content = it },
                        placeholder   = { Text("Say something spicy…") },
                        modifier      = Modifier.weight(1f),
                        maxLines      = 4
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick  = { viewModel.postRoast("match", "general", content.trim()) },
                        enabled  = content.isNotBlank(),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Post",
                            tint = if (content.isNotBlank()) GreenPrimary
                                   else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Divider()

            // Feed
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (roastsState) {
                    is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    is Resource.Success -> {
                        val roasts = (roastsState as Resource.Success).data ?: emptyList()
                        if (roasts.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No roasts yet. Be the first!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                contentPadding      = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier            = Modifier.fillMaxSize()
                            ) {
                                items(roasts, key = { it.id }) { roast ->
                                    RoastCard(
                                        roast         = roast,
                                        isOwner       = roast.author.id == currentUserId,
                                        onDeleteClick = { roastToDelete = roast }
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((roastsState as Resource.Error).message ?: "Error",
                            color = MaterialTheme.colorScheme.error)
                    }
                    null -> {}
                }
            }
        }

        SnackbarHost(hostState = snackbarHost, modifier = Modifier.align(Alignment.BottomCenter))
    }

    // Delete confirmation
    roastToDelete?.let { roast ->
        AlertDialog(
            onDismissRequest = { roastToDelete = null },
            title = { Text("Delete Roast") },
            text  = { Text("Delete this roast?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteRoast(roast.id); roastToDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { roastToDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun RoastCard(roast: Roast, isOwner: Boolean, onDeleteClick: () -> Unit) {
    Card(
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = GreenPrimary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text  = roast.author.name.firstOrNull()?.toString() ?: "?",
                                color = androidx.compose.ui.graphics.Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(roast.author.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold)
                        Text(roast.createdAt.take(10),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (isOwner) {
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(roast.content, style = MaterialTheme.typography.bodyMedium)
            if (roast.likes > 0) {
                Spacer(Modifier.height(4.dp))
                Text("${roast.likes} likes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
