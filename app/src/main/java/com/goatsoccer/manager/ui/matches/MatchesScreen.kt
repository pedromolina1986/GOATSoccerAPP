@file:OptIn(ExperimentalMaterial3Api::class)

package com.goatsoccer.manager.ui.matches

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goatsoccer.manager.data.model.GoalEvent
import com.goatsoccer.manager.data.model.League
import com.goatsoccer.manager.data.model.Match
import com.goatsoccer.manager.data.model.Player
import com.goatsoccer.manager.data.model.Team
import com.goatsoccer.manager.ui.theme.GreenDark
import com.goatsoccer.manager.ui.theme.GreenPrimary
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.goatsoccer.manager.util.Resource
import com.goatsoccer.manager.util.appViewModel

private const val SCREEN_TAG = "MatchesScreen"

sealed class MatchListItem {
    data class Header(val title: String) : MatchListItem()
    data class MatchEntry(val match: Match) : MatchListItem()
}

@Composable
fun MatchesScreen(
    isCoach: Boolean = false,
    viewModel: MatchesViewModel = appViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.silentRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val matchesState      by viewModel.matches.observeAsState()
    val leagues           by viewModel.leagues.observeAsState(emptyList())
    val teams             by viewModel.teams.observeAsState(emptyList())
    val allPlayers        by viewModel.allPlayers.observeAsState(emptyList())
    val selectedLeagueId  by viewModel.selectedLeagueId.observeAsState()
    val selectedTeamId    by viewModel.selectedTeamId.observeAsState()

    var showAddDialog     by remember { mutableStateOf(false) }
    var matchToDelete     by remember { mutableStateOf<Match?>(null) }
    var goalPickerState   by remember { mutableStateOf<Pair<Match, String>?>(null) }

    SideEffect {
        Log.d(SCREEN_TAG, "recompose — matchesState=${matchesState?.let { it::class.simpleName }}, " +
            "leagues=${leagues.size}, players=${allPlayers.size}")
    }

    val isRefreshing = matchesState is Resource.Loading
    val errorMessage = (matchesState as? Resource.Error)?.message
    val allMatches   = (matchesState as? Resource.Success)?.data ?: emptyList()

    // Build grouped list exactly as the Fragment did
    // Teams that actually appear in the current match list (for the team filter chips)
    val teamsInMatches = remember(allMatches, selectedLeagueId) {
        val filtered = if (selectedLeagueId == null) allMatches
                       else allMatches.filter { it.leagueId == selectedLeagueId }
        val ids = filtered.flatMap { listOf(it.homeTeam.id, it.awayTeam.id) }.toSet()
        teams.filter { it.id in ids }
    }

    val listItems = remember(allMatches, leagues, selectedLeagueId, selectedTeamId) {
        buildMatchItems(allMatches, leagues, selectedLeagueId, selectedTeamId)
    }

    val snackbarHost = remember { SnackbarHostState() }

    // No inner Scaffold — use Box overlay to avoid nested-Scaffold crashes
    Box(Modifier.fillMaxSize()) {

        Column(Modifier.fillMaxSize()) {

            // League filter chips
            if (leagues.isNotEmpty()) {
                LeagueChipRow(
                    leagues          = leagues,
                    selectedLeagueId = selectedLeagueId,
                    onSelect         = { viewModel.selectLeague(it) }
                )
            }

            // Team filter chips (only when there are multiple teams in the list)
            if (teamsInMatches.size > 1) {
                TeamChipRow(
                    teams          = teamsInMatches,
                    selectedTeamId = selectedTeamId,
                    onSelect       = { viewModel.selectTeam(it) }
                )
            }

            // Pull-to-refresh indicator
            if (isRefreshing) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    isRefreshing -> { /* spinner shown above */ }
                    errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                    listItems.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matches found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    else -> LazyColumn(
                        contentPadding = PaddingValues(
                            start  = 12.dp, end = 12.dp,
                            top    = 8.dp,
                            bottom = if (isCoach) 80.dp else 8.dp  // space for FAB
                        ),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(listItems, key = { index, item ->
                            when (item) {
                                is MatchListItem.Header     -> "h_$index"
                                is MatchListItem.MatchEntry -> "m_${item.match.id}"
                            }
                        }) { _, item ->
                            when (item) {
                                is MatchListItem.Header ->
                                    SectionHeader(item.title)
                                is MatchListItem.MatchEntry ->
                                    MatchCard(
                                        match         = item.match,
                                        isCoach       = isCoach,
                                        onAddHomeGoal = { goalPickerState = item.match to item.match.homeTeam.id },
                                        onAddAwayGoal = { goalPickerState = item.match to item.match.awayTeam.id },
                                        onRemoveGoal  = { idx -> viewModel.removeGoal(item.match.id, idx) },
                                        onFinalize    = { viewModel.setMatchStatus(item.match.id, "finished") },
                                        onReopen      = { viewModel.setMatchStatus(item.match.id, "scheduled") },
                                        onDelete      = { matchToDelete = item.match }
                                    )
                            }
                        }
                    }
                }
            }
        }

        // FAB — overlaid at bottom-end (coach only)
        if (isCoach) {
            FloatingActionButton(
                onClick        = { showAddDialog = true },
                containerColor = GreenPrimary,
                modifier       = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Match", tint = Color.White)
            }
        }

        // Snackbar — overlaid at bottom-center
        SnackbarHost(
            hostState = snackbarHost,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Delete confirmation
    matchToDelete?.let { match ->
        AlertDialog(
            onDismissRequest = { matchToDelete = null },
            title = { Text("Delete Match") },
            text  = { Text("Delete ${match.homeTeam.name} vs ${match.awayTeam.name}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteMatch(match.id); matchToDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { matchToDelete = null }) { Text("Cancel") } }
        )
    }

    // Goal scorer picker
    goalPickerState?.let { (match, teamId) ->
        val players = allPlayers.filter { it.teamId == teamId }
        val teamName = if (teamId == match.homeTeam.id) match.homeTeam.name else match.awayTeam.name
        if (players.isEmpty()) {
            LaunchedEffect(Unit) {
                snackbarHost.showSnackbar("No players for $teamName")
                goalPickerState = null
            }
        } else {
            AlertDialog(
                onDismissRequest = { goalPickerState = null },
                title = { Text("Goal scorer — $teamName") },
                text = {
                    Column {
                        players.forEach { p ->
                            TextButton(
                                onClick = {
                                    viewModel.addGoal(match.id, GoalEvent(p.id, p.name, p.teamId))
                                    goalPickerState = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(p.name, modifier = Modifier.fillMaxWidth()) }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { goalPickerState = null }) { Text("Cancel") } }
            )
        }
    }

    // Add match dialog
    if (showAddDialog) {
        AddMatchDialog(
            teams   = teams,
            leagues = leagues,
            onConfirm = { homeId, awayId, date, time, location, leagueId ->
                showAddDialog = false
                viewModel.createMatch(homeId, awayId, date, time, location, leagueId)
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

// ── Helpers: grouping logic (mirrors MatchesFragment.renderMatches) ───────────

private fun buildMatchItems(
    matches: List<Match>,
    leagues: List<League>,
    selectedLeagueId: String?,
    selectedTeamId: String? = null
): List<MatchListItem> {
    var filtered = if (selectedLeagueId == null) matches
                   else matches.filter { it.leagueId == selectedLeagueId }
    if (selectedTeamId != null) {
        filtered = filtered.filter {
            it.homeTeam.id == selectedTeamId || it.awayTeam.id == selectedTeamId
        }
    }

    val items = mutableListOf<MatchListItem>()

    if (selectedLeagueId == null) {
        leagues.filter { league -> filtered.any { it.leagueId == league.id } }.forEach { league ->
            val lm       = filtered.filter { it.leagueId == league.id }
            val upcoming = lm.filter { it.status != "finished" }.sortedBy { it.date }
            val results  = lm.filter { it.status == "finished" }.sortedByDescending { it.date }
            items.add(MatchListItem.Header(league.name))
            if (upcoming.isNotEmpty()) {
                items.add(MatchListItem.Header("  Upcoming (${upcoming.size})"))
                upcoming.forEach { items.add(MatchListItem.MatchEntry(it)) }
            }
            if (results.isNotEmpty()) {
                items.add(MatchListItem.Header("  Results (${results.size})"))
                results.forEach { items.add(MatchListItem.MatchEntry(it)) }
            }
        }
        val unassigned = filtered.filter { m -> leagues.none { it.id == m.leagueId } }
        if (unassigned.isNotEmpty()) {
            items.add(MatchListItem.Header("Other Matches"))
            unassigned.sortedBy { it.date }.forEach { items.add(MatchListItem.MatchEntry(it)) }
        }
    } else {
        val upcoming = filtered.filter { it.status != "finished" }.sortedBy { it.date }
        val results  = filtered.filter { it.status == "finished" }.sortedByDescending { it.date }
        if (upcoming.isNotEmpty()) {
            items.add(MatchListItem.Header("Upcoming (${upcoming.size})"))
            upcoming.forEach { items.add(MatchListItem.MatchEntry(it)) }
        }
        if (results.isNotEmpty()) {
            items.add(MatchListItem.Header("Results (${results.size})"))
            results.forEach { items.add(MatchListItem.MatchEntry(it)) }
        }
    }
    return items
}

// ── Composable pieces ─────────────────────────────────────────────────────────

@Composable
fun LeagueChipRow(
    leagues: List<League>,
    selectedLeagueId: String?,
    onSelect: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedLeagueId == null,
            onClick  = { onSelect(null) },
            label    = { Text("All") },
            colors   = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GreenPrimary,
                selectedLabelColor     = Color.White
            )
        )
        leagues.forEach { league ->
            FilterChip(
                selected = selectedLeagueId == league.id,
                onClick  = { onSelect(league.id) },
                label    = { Text(league.name) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenPrimary,
                    selectedLabelColor     = Color.White
                )
            )
        }
    }
}

@Composable
fun TeamChipRow(
    teams: List<com.goatsoccer.manager.data.model.Team>,
    selectedTeamId: String?,
    onSelect: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedTeamId == null,
            onClick  = { onSelect(null) },
            label    = { Text("All Teams") },
            colors   = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GreenPrimary,
                selectedLabelColor     = Color.White
            )
        )
        teams.forEach { team ->
            FilterChip(
                selected = selectedTeamId == team.id,
                onClick  = { onSelect(team.id) },
                label    = { Text(team.name) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenPrimary,
                    selectedLabelColor     = Color.White
                )
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelLarge,
        color    = GreenDark,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    )
}

@Composable
private fun MatchCard(
    match: Match,
    isCoach: Boolean,
    onAddHomeGoal: () -> Unit,
    onAddAwayGoal: () -> Unit,
    onRemoveGoal: (Int) -> Unit,
    onFinalize: () -> Unit,
    onReopen: () -> Unit,
    onDelete: () -> Unit
) {
    val isFinished  = match.status == "finished"
    val statusColor = when (match.status) {
        "finished" -> Color(0xFF388E3C)
        "live"     -> Color(0xFFD32F2F)
        else       -> Color(0xFF1565C0)
    }
    val statusLabel = match.status.uppercase()

    Card(
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            // Status badge + finalize/reopen chip + delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = statusColor
                ) {
                    Text("  $statusLabel  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White)
                }
                if (isCoach) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isFinished) {
                            TextButton(
                                onClick        = onReopen,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier       = Modifier.height(28.dp)
                            ) { Text("Reopen", fontSize = 11.sp, color = Color(0xFF1565C0)) }
                        } else {
                            TextButton(
                                onClick        = onFinalize,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier       = Modifier.height(28.dp)
                            ) { Text("Finalize", fontSize = 11.sp, color = Color(0xFF388E3C)) }
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Row 1: team names + score — always on the same line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = match.homeTeam.name,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 1,
                    overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    textAlign  = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier   = Modifier.weight(1f)
                )
                Text(
                    text       = "${match.homeScore} – ${match.awayScore}",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = GreenDark,
                    modifier   = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text       = match.awayTeam.name,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 1,
                    overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    textAlign  = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier   = Modifier.weight(1f)
                )
            }

            // Row 2: goal buttons + scorers (coach only, or when there are scorers)
            val homeScorers = match.goalScorers.filter { it.teamId == match.homeTeam.id }
            val awayScorers = match.goalScorers.filter { it.teamId == match.awayTeam.id }
            if (isCoach || homeScorers.isNotEmpty() || awayScorers.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier            = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isCoach) {
                            TextButton(
                                onClick        = onAddHomeGoal,
                                contentPadding = PaddingValues(0.dp),
                                modifier       = Modifier.height(28.dp)
                            ) { Text("+ Goal", fontSize = 11.sp) }
                        }
                        homeScorers.forEach { goal ->
                            Text(
                                text     = goal.playerName + if (isCoach) "  ✕" else "",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = if (isCoach) Modifier.clickable {
                                    onRemoveGoal(match.goalScorers.indexOf(goal))
                                } else Modifier
                            )
                        }
                    }
                    Spacer(Modifier.width(64.dp)) // aligns with the score column above
                    Column(
                        modifier            = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isCoach) {
                            TextButton(
                                onClick        = onAddAwayGoal,
                                contentPadding = PaddingValues(0.dp),
                                modifier       = Modifier.height(28.dp)
                            ) { Text("+ Goal", fontSize = 11.sp) }
                        }
                        awayScorers.forEach { goal ->
                            Text(
                                text     = goal.playerName + if (isCoach) "  ✕" else "",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = if (isCoach) Modifier.clickable {
                                    onRemoveGoal(match.goalScorers.indexOf(goal))
                                } else Modifier
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text("${match.date}  ${match.time}  •  ${match.location}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AddMatchDialog(
    teams: List<Team>,
    leagues: List<League>,
    onConfirm: (String, String, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var homeIdx    by remember { mutableIntStateOf(0) }
    var awayIdx    by remember { mutableIntStateOf(if (teams.size > 1) 1 else 0) }
    var leagueIdx  by remember { mutableIntStateOf(0) }
    var date       by remember { mutableStateOf("") }
    var time       by remember { mutableStateOf("") }
    var location   by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Match") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (teams.isEmpty()) {
                    Text("No teams available. Create teams first.",
                        color = MaterialTheme.colorScheme.error)
                } else {
                    Text("Home Team", style = MaterialTheme.typography.labelMedium)
                    TeamDropdown(teams, homeIdx) { homeIdx = it }

                    Text("Away Team", style = MaterialTheme.typography.labelMedium)
                    TeamDropdown(teams, awayIdx) { awayIdx = it }

                    if (leagues.isNotEmpty()) {
                        Text("League", style = MaterialTheme.typography.labelMedium)
                        LeagueDropdown(leagues, leagueIdx) { leagueIdx = it }
                    }

                    OutlinedTextField(value = date,     onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = time,     onValueChange = { time = it },
                        label = { Text("Time (HH:MM)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = location, onValueChange = { location = it },
                        label = { Text("Location") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (teams.isEmpty()) return@TextButton
                    val leagueId = leagues.getOrNull(leagueIdx)?.id ?: ""
                    onConfirm(teams[homeIdx].id, teams[awayIdx].id, date.trim(), time.trim(), location.trim(), leagueId)
                }
            ) { Text("Schedule") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun TeamDropdown(teams: List<Team>, selectedIdx: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value          = teams.getOrNull(selectedIdx)?.name ?: "",
            onValueChange  = {},
            readOnly       = true,
            trailingIcon   = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier       = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            teams.forEachIndexed { idx, team ->
                DropdownMenuItem(text = { Text(team.name) }, onClick = { onSelect(idx); expanded = false })
            }
        }
    }
}

@Composable
private fun LeagueDropdown(leagues: List<League>, selectedIdx: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value         = leagues.getOrNull(selectedIdx)?.let { "${it.name} (${it.season})" } ?: "",
            onValueChange = {},
            readOnly      = true,
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier      = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            leagues.forEachIndexed { idx, league ->
                DropdownMenuItem(
                    text    = { Text("${league.name} (${league.season})") },
                    onClick = { onSelect(idx); expanded = false }
                )
            }
        }
    }
}

