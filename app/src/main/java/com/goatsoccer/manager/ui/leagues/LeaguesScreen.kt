package com.goatsoccer.manager.ui.leagues

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.goatsoccer.manager.data.model.League
import com.goatsoccer.manager.ui.theme.GreenDark
import com.goatsoccer.manager.ui.theme.GreenPrimary
import com.goatsoccer.manager.util.Resource
import com.goatsoccer.manager.util.appViewModel

@Composable
fun LeaguesScreen(
    isCoach: Boolean = false,
    viewModel: LeaguesViewModel = appViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.silentRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val leaguesState by viewModel.leagues.observeAsState()
    val teams        by viewModel.teams.observeAsState(emptyList())
    val actionResult by viewModel.actionResult.observeAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var leagueToDelete   by remember { mutableStateOf<League?>(null) }
    var addTeamToLeague  by remember { mutableStateOf<League?>(null) }
    var confirmRemove    by remember { mutableStateOf<Pair<League, String>?>(null) }

    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(actionResult) {
        if (actionResult is Resource.Error)
            snackbarHost.showSnackbar((actionResult as Resource.Error).message ?: "Error")
    }

    Scaffold(
        floatingActionButton = {
            if (isCoach) {
                FloatingActionButton(
                    onClick        = { showCreateDialog = true },
                    containerColor = GreenPrimary
                ) {
                    Icon(Icons.Default.Add, "Create League", tint = Color.White)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        val isLoading = leaguesState is Resource.Loading
        val leagues   = (leaguesState as? Resource.Success)?.data ?: emptyList()

        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color    = GreenPrimary
                )
                leagues.isEmpty() -> Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier           = Modifier.size(48.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text  = if (isCoach) "No leagues yet.\nTap + to create one." else "No leagues available.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(leagues, key = { it.id }) { league ->
                        LeagueCard(
                            league       = league,
                            teamNameFn   = { viewModel.teamNameById(it) },
                            isCoach      = isCoach,
                            onDelete     = { leagueToDelete = league },
                            onAddTeam    = { addTeamToLeague = league },
                            onRemoveTeam = { teamId -> confirmRemove = league to teamId }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateLeagueDialog(
            onConfirm = { name, season -> showCreateDialog = false; viewModel.createLeague(name, season) },
            onDismiss = { showCreateDialog = false }
        )
    }

    leagueToDelete?.let { league ->
        AlertDialog(
            onDismissRequest = { leagueToDelete = null },
            title = { Text("Delete League") },
            text  = { Text("Delete \"${league.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteLeague(league.id); leagueToDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { leagueToDelete = null }) { Text("Cancel") } }
        )
    }

    addTeamToLeague?.let { league ->
        val available = teams.filter { it.id !in league.teamIds }
        if (available.isEmpty()) {
            LaunchedEffect(Unit) {
                snackbarHost.showSnackbar("All teams already in this league")
                addTeamToLeague = null
            }
        } else {
            AlertDialog(
                onDismissRequest = { addTeamToLeague = null },
                title = { Text("Add Team to ${league.name}") },
                text  = {
                    Column {
                        available.forEach { team ->
                            TextButton(
                                onClick  = { viewModel.addTeam(league.id, team.id); addTeamToLeague = null },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(team.name, modifier = Modifier.fillMaxWidth()) }
                        }
                    }
                },
                confirmButton  = {},
                dismissButton  = { TextButton(onClick = { addTeamToLeague = null }) { Text("Cancel") } }
            )
        }
    }

    confirmRemove?.let { (league, teamId) ->
        AlertDialog(
            onDismissRequest = { confirmRemove = null },
            title = { Text("Remove Team") },
            text  = { Text("Remove ${viewModel.teamNameById(teamId)} from ${league.name}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.removeTeam(league.id, teamId); confirmRemove = null }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmRemove = null }) { Text("Cancel") } }
        )
    }
}

// ── League Card ───────────────────────────────────────────────────────────────

@Composable
private fun LeagueCard(
    league: League,
    teamNameFn: (String) -> String,
    isCoach: Boolean,
    onDelete: () -> Unit,
    onAddTeam: () -> Unit,
    onRemoveTeam: (String) -> Unit
) {
    Card(
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column {
            // ── Gradient header ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(GreenDark, GreenPrimary)))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Trophy avatar
                    Box(
                        modifier         = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text       = league.name,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape    = MaterialTheme.shapes.small,
                            color    = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text     = league.season,
                                style    = MaterialTheme.typography.labelSmall,
                                color    = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (isCoach) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint               = Color.White.copy(alpha = 0.85f),
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // ── Teams section ─────────────────────────────────────────────
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.Default.Group,
                        contentDescription = null,
                        tint               = GreenPrimary,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = "${league.teamIds.size} team${if (league.teamIds.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (league.teamIds.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    Spacer(Modifier.height(10.dp))

                    league.teamIds.forEach { teamId ->
                        TeamRow(
                            name     = teamNameFn(teamId),
                            isCoach  = isCoach,
                            onRemove = { onRemoveTeam(teamId) }
                        )
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = "No teams added yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isCoach) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick  = onAddTeam,
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Team", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamRow(name: String, isCoach: Boolean, onRemove: () -> Unit) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                color      = GreenPrimary
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text       = name,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.weight(1f)
        )
        if (isCoach) {
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ── Create dialog ─────────────────────────────────────────────────────────────

@Composable
private fun CreateLeagueDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name   by remember { mutableStateOf("") }
    var season by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create League") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("League Name") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = season,
                    onValueChange = { season = it },
                    label         = { Text("Season (e.g. 2025)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick  = { if (name.isNotBlank()) onConfirm(name.trim(), season.trim()) },
                enabled  = name.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
