@file:OptIn(ExperimentalMaterial3Api::class)

package com.goatsoccer.manager.ui.teams

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goatsoccer.manager.data.model.Player
import com.goatsoccer.manager.data.model.PlayerLookupResult
import com.goatsoccer.manager.ui.theme.GreenPrimary
import com.goatsoccer.manager.util.Resource
import com.goatsoccer.manager.util.appViewModel

// ── Teams Detail (Coach view) ─────────────────────────────────────────────────

@Composable
fun TeamDetailScreen(
    teamId: String,
    isCoach: Boolean,
    viewModel: TeamDetailViewModel = appViewModel()
) {
    LaunchedEffect(teamId) {
        viewModel.loadTeam(teamId)
        viewModel.loadPlayers(teamId)
    }

    val teamResult    by viewModel.team.observeAsState()
    val playersResult by viewModel.players.observeAsState()
    val snackbarHost  = remember { SnackbarHostState() }
    var showInviteDialog by remember { mutableStateOf(false) }
    var playerOptions    by remember { mutableStateOf<Player?>(null) }

    val addPlayerResult by viewModel.addPlayerResult.observeAsState()
    val inviteResult    by viewModel.inviteResult.observeAsState()
    val actionResult    by viewModel.actionResult.observeAsState()

    LaunchedEffect(addPlayerResult) {
        when (addPlayerResult) {
            is Resource.Success -> snackbarHost.showSnackbar("Player added!")
            is Resource.Error   -> snackbarHost.showSnackbar((addPlayerResult as Resource.Error).message ?: "Error")
            else -> {}
        }
    }
    LaunchedEffect(actionResult) {
        when (actionResult) {
            is Resource.Success -> snackbarHost.showSnackbar("Done")
            is Resource.Error   -> snackbarHost.showSnackbar((actionResult as Resource.Error).message ?: "Error")
            else -> {}
        }
    }

    val team    = (teamResult as? Resource.Success)?.data
    val players = (playersResult as? Resource.Success)?.data ?: emptyList()
    val captain = players.firstOrNull { it.isCaptain }

    Scaffold(
        floatingActionButton = {
            if (isCoach) {
                FloatingActionButton(
                    onClick        = { showInviteDialog = true },
                    containerColor = GreenPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Player", tint = Color.White)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start  = 16.dp,
                end    = 16.dp,
                top    = 16.dp,
                bottom = 16.dp + padding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = padding.calculateTopPadding(), bottom = 0.dp)
        ) {
            // Team info card
            item {
                Card(
                    colors    = CardDefaults.cardColors(containerColor = GreenPrimary),
                    modifier  = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(team?.name ?: "Loading…",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(team?.city ?: "",
                            style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                        Text("Coach: ${team?.coach ?: ""}",
                            style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                        Text("W ${team?.wins ?: 0}  D ${team?.draws ?: 0}  L ${team?.losses ?: 0}  |  ${team?.points ?: 0} pts",
                            style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
            // Player count / captain line
            item {
                Text(
                    text = "${players.size} Players" +
                        if (captain != null) "  •  Captain: ${captain.name}" else "",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Loading indicator
            if (playersResult is Resource.Loading) {
                item { CircularProgressIndicator(Modifier.padding(16.dp)) }
            }
            // Player rows
            items(players, key = { it.id }) { player ->
                PlayerCard(
                    player   = player,
                    isCoach  = isCoach,
                    onClick  = { playerOptions = player }
                )
            }
        }
    }

    // Player options dialog (coach: set captain / remove; player: stats only)
    playerOptions?.let { player ->
        if (isCoach) {
            val captainLabel = if (player.isCaptain) "Remove Captain" else "Set as Captain"
            AlertDialog(
                onDismissRequest = { playerOptions = null },
                title = { Text(player.name) },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                viewModel.setCaptain(if (player.isCaptain) "" else player.id)
                                playerOptions = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(captainLabel, modifier = Modifier.fillMaxWidth()) }

                        TextButton(
                            onClick = { viewModel.deletePlayer(teamId, player.id); playerOptions = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Remove from Team",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth())
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { playerOptions = null }) { Text("Cancel") }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { playerOptions = null },
                title = { Text("${player.name}  #${player.number}") },
                text  = {
                    Text(
                        "Position: ${player.position}\n" +
                        "Goals: ${player.goals}  •  Assists: ${player.assists}\n" +
                        "Yellow cards: ${player.yellowCards}"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { playerOptions = null }) { Text("OK") }
                }
            )
        }
    }

    // Invite / Add player dialog (coach only)
    if (showInviteDialog) {
        InvitePlayerDialog(
            viewModel = viewModel,
            teamId    = teamId,
            onDismiss = { showInviteDialog = false }
        )
    }
}

// ── MyTeam (Player view) ──────────────────────────────────────────────────────

@Composable
fun MyTeamScreen(viewModel: TeamDetailViewModel = appViewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val teamId  = remember { com.goatsoccer.manager.data.local.SessionManager(context).getMyTeamId().ifBlank { "t1" } }

    LaunchedEffect(teamId) {
        viewModel.loadTeam(teamId)
        viewModel.loadPlayers(teamId)
    }

    // Reuse TeamDetailScreen in read-only mode
    TeamDetailScreen(teamId = teamId, isCoach = false, viewModel = viewModel)
}

// ── Shared composables ────────────────────────────────────────────────────────

@Composable
fun PlayerCard(player: Player, isCoach: Boolean, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick   = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = GreenPrimary.copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = GreenPrimary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(player.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (player.isCaptain) {
                        Spacer(Modifier.width(6.dp))
                        Surface(shape = MaterialTheme.shapes.extraSmall, color = GreenPrimary) {
                            Text("  C  ", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
                Text("#${player.number}  ${player.position}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("G: ${player.goals}  A: ${player.assists}",
                    style = MaterialTheme.typography.bodySmall)
                Text("YC: ${player.yellowCards}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Invite Player Dialog (multi-step) ─────────────────────────────────────────

private enum class InviteStep { IDLE, LOADING, FOUND, NOT_FOUND }

@Composable
private fun InvitePlayerDialog(
    viewModel: TeamDetailViewModel,
    teamId: String,
    onDismiss: () -> Unit
) {
    var email        by remember { mutableStateOf("") }
    var inviteName   by remember { mutableStateOf("") }
    var position     by remember { mutableStateOf("") }
    var number       by remember { mutableStateOf("") }
    var step         by remember { mutableStateOf(InviteStep.IDLE) }

    val lookupResult by viewModel.lookupResult.observeAsState()
    val inviteResult by viewModel.inviteResult.observeAsState()

    // React to lookup result
    LaunchedEffect(lookupResult) {
        step = when (lookupResult) {
            is Resource.Loading -> InviteStep.LOADING
            is Resource.Success -> {
                val data = (lookupResult as Resource.Success<PlayerLookupResult>).data!!
                if (data.found && data.user != null) InviteStep.FOUND else InviteStep.NOT_FOUND
            }
            is Resource.Error   -> InviteStep.IDLE
            null -> InviteStep.IDLE
        }
    }

    LaunchedEffect(inviteResult) {
        if (inviteResult is Resource.Success) onDismiss()
    }

    val foundUser = (lookupResult as? Resource.Success<PlayerLookupResult>)?.data?.user

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add / Invite Player") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it },
                    label         = { Text("Email") },
                    singleLine    = true,
                    enabled       = step == InviteStep.IDLE,
                    modifier      = Modifier.fillMaxWidth()
                )

                when (step) {
                    InviteStep.LOADING -> LinearProgressIndicator(Modifier.fillMaxWidth())

                    InviteStep.FOUND -> foundUser?.let { user ->
                        Card(colors = CardDefaults.cardColors(containerColor = GreenPrimary.copy(alpha = 0.1f))) {
                            Column(Modifier.padding(12.dp)) {
                                Text(user.name, fontWeight = FontWeight.Bold)
                                Text(user.role, style = MaterialTheme.typography.bodySmall)
                                Text(user.email, style = MaterialTheme.typography.bodySmall)
                                if (!user.teamId.isNullOrBlank()) {
                                    Text("Already on a team", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    InviteStep.NOT_FOUND -> {
                        Text("No existing user found. Fill in details to send an invite:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(value = inviteName, onValueChange = { inviteName = it },
                            label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = position, onValueChange = { position = it },
                            label = { Text("Position") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = number, onValueChange = { number = it },
                            label = { Text("Jersey #") }, singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth())
                    }

                    else -> {}
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (step) {
                        InviteStep.IDLE      -> viewModel.lookupEmail(teamId, email.trim())
                        InviteStep.FOUND     -> viewModel.invitePlayer(teamId, email.trim(), "", "", 0)
                        InviteStep.NOT_FOUND -> viewModel.invitePlayer(
                            teamId, email.trim(),
                            inviteName.trim(), position.trim(), number.toIntOrNull() ?: 0
                        )
                        else -> {}
                    }
                },
                enabled = step != InviteStep.LOADING
            ) {
                Text(when (step) {
                    InviteStep.IDLE      -> "Look Up"
                    InviteStep.FOUND     -> "Attach Player"
                    InviteStep.NOT_FOUND -> "Send Invite"
                    InviteStep.LOADING   -> "…"
                })
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
