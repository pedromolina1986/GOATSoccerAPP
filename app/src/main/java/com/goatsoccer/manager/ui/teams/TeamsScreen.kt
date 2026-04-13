package com.goatsoccer.manager.ui.teams

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goatsoccer.manager.data.model.League
import com.goatsoccer.manager.data.model.Team
import com.goatsoccer.manager.ui.theme.GreenDark
import com.goatsoccer.manager.ui.theme.GreenPrimary
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.goatsoccer.manager.util.Resource
import com.goatsoccer.manager.util.appViewModel

@Composable
fun TeamsScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: TeamsViewModel = appViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.silentRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val teamsState   by viewModel.teams.observeAsState()
    val leagues      by viewModel.leagues.observeAsState(emptyList())
    val createResult by viewModel.createResult.observeAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var teamToDelete  by remember { mutableStateOf<Team?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(createResult) {
        when (createResult) {
            is Resource.Success -> { snackbarHostState.showSnackbar("Team created!"); viewModel.fetchTeams() }
            is Resource.Error   -> snackbarHostState.showSnackbar((createResult as Resource.Error).message ?: "Error")
            else -> {}
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showAddDialog = true },
                containerColor = GreenPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Team", tint = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val isLoading = teamsState is Resource.Loading
        val teams = (teamsState as? Resource.Success)?.data ?: emptyList()

        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                teams.isEmpty() -> Text(
                    "No teams yet. Tap + to create one.",
                    modifier = Modifier.align(Alignment.Center),
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(teams, key = { it.id }) { team ->
                        TeamCard(
                            team     = team,
                            leagues  = leagues.filter { team.id in it.teamIds },
                            onClick  = { onNavigateToDetail(team.id) },
                            onDelete = { teamToDelete = team }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTeamDialog(
            onConfirm = { name, city, coach ->
                showAddDialog = false
                viewModel.createTeam(name, city, coach)
            },
            onDismiss = { showAddDialog = false }
        )
    }

    teamToDelete?.let { team ->
        AlertDialog(
            onDismissRequest = { teamToDelete = null },
            title = { Text("Delete Team") },
            text  = { Text("Are you sure you want to delete ${team.name}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTeam(team.id); teamToDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { teamToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TeamCard(
    team: Team,
    leagues: List<League>,   // kept for API compatibility but data now comes from team.leagueStats
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(3.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // ── Header strip ──────────────────────────────────────────────
            Surface(color = GreenPrimary.copy(alpha = 0.10f)) {
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Surface(
                        shape    = CircleShape,
                        color    = GreenPrimary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text       = team.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                color      = Color.White,
                                fontSize   = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text       = team.name,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = GreenDark
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector   = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint          = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier      = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text  = team.city,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint     = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ── Body ──────────────────────────────────────────────────────
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {

                // Coach row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.Person,
                        contentDescription = null,
                        tint               = GreenPrimary,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = team.coach,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Players count row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Group,
                        contentDescription = null,
                        tint               = GreenPrimary,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = "${team.playerCount} player${if (team.playerCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // League stats table
                if (team.leagueStats.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    LeagueStatsTable(stats = team.leagueStats)
                }
            }
        }
    }
}

@Composable
private fun LeagueStatsTable(stats: List<com.goatsoccer.manager.data.model.TeamLeagueStat>) {
    val columns = listOf("League", "#", "P", "W", "D", "L", "GF", "GA", "Pts")
    val colWeights = listOf(2.2f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.6f, 0.6f, 0.6f)

    val headerStyle = MaterialTheme.typography.labelSmall
    val cellStyle   = MaterialTheme.typography.bodySmall

    // Header
    Row(Modifier.fillMaxWidth()) {
        columns.forEachIndexed { i, col ->
            Text(
                text      = col,
                style     = headerStyle,
                fontWeight = FontWeight.SemiBold,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier  = Modifier.weight(colWeights[i])
            )
        }
    }

    Spacer(Modifier.height(4.dp))
    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

    // League rows
    stats.forEach { s ->
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text     = "${s.leagueName}\n${s.season}",
                style    = cellStyle,
                color    = GreenDark,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(colWeights[0])
            )
            listOf(s.rank, s.played, s.won, s.drawn, s.lost, s.goalsFor, s.goalsAgainst, s.points)
                .forEachIndexed { i, v ->
                    val col = columns[i + 1]
                    Text(
                        text       = v.toString(),
                        style      = cellStyle,
                        color      = when (col) {
                            "#"   -> MaterialTheme.colorScheme.onSurfaceVariant
                            "Pts" -> GreenDark
                            else  -> MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = if (col == "Pts" || col == "#") FontWeight.Bold else FontWeight.Normal,
                        textAlign  = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier   = Modifier.weight(colWeights[i + 1])
                    )
                }
        }
    }

    // Totals row
    if (stats.size > 1) {
        val total = stats.reduce { acc, s -> acc.copy(
            leagueName   = "Total",
            season       = "",
            played       = acc.played       + s.played,
            won          = acc.won          + s.won,
            drawn        = acc.drawn        + s.drawn,
            lost         = acc.lost         + s.lost,
            goalsFor     = acc.goalsFor     + s.goalsFor,
            goalsAgainst = acc.goalsAgainst + s.goalsAgainst,
            points       = acc.points       + s.points
        )}
        Spacer(Modifier.height(4.dp))
        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text       = "Total",
                style      = cellStyle,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
                modifier   = Modifier.weight(colWeights[0])
            )
            // index 0 = "#" (rank — not summed), rest are numeric totals
            val totalValues = listOf("—", total.played, total.won, total.drawn, total.lost,
                                     total.goalsFor, total.goalsAgainst, total.points)
            totalValues.forEachIndexed { i, v ->
                val col = columns[i + 1]
                Text(
                    text       = v.toString(),
                    style      = cellStyle,
                    fontWeight = FontWeight.Bold,
                    color      = if (col == "Pts") GreenDark else MaterialTheme.colorScheme.onSurface,
                    textAlign  = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier   = Modifier.weight(colWeights[i + 1])
                )
            }
        }
    }
}

@Composable
private fun AddTeamDialog(onConfirm: (String, String, String) -> Unit, onDismiss: () -> Unit) {
    var name  by remember { mutableStateOf("") }
    var city  by remember { mutableStateOf("") }
    var coach by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Team") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name,  onValueChange = { name  = it }, label = { Text("Team Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = city,  onValueChange = { city  = it }, label = { Text("City") },      singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = coach, onValueChange = { coach = it }, label = { Text("Coach") },     singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim(), city.trim(), coach.trim()) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
