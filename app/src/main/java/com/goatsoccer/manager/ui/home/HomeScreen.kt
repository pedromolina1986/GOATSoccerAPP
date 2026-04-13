package com.goatsoccer.manager.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.goatsoccer.manager.data.local.SessionManager
import com.goatsoccer.manager.data.model.Match
import com.goatsoccer.manager.data.model.Player
import com.goatsoccer.manager.data.model.Standing
import com.goatsoccer.manager.ui.theme.GreenDark
import com.goatsoccer.manager.ui.theme.GreenPrimary
import com.goatsoccer.manager.util.Resource
import com.goatsoccer.manager.util.appViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = appViewModel()) {
    val context       = LocalContext.current
    val userName      = remember { SessionManager(context).getUserName() ?: "there" }
    val role          = remember { SessionManager(context).getUserRole() ?: "fan" }
    val state         by viewModel.dashboard.observeAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.silentRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Hero header ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GreenDark, GreenPrimary)))
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column {
                Text(
                    text       = "Hello, $userName",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
                Text(
                    text  = role.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
            IconButton(
                onClick  = { viewModel.loadDashboard() },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
            }
        }

        when (state) {
            is Resource.Loading, null -> {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }
            is Resource.Error -> {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text((state as Resource.Error).message ?: "Error", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadDashboard() }) { Text("Retry") }
                    }
                }
            }
            is Resource.Success -> {
                val d = (state as Resource.Success<DashboardData>).data!!
                DashboardContent(d)
            }
        }
    }
}

@Composable
private fun DashboardContent(d: DashboardData) {
    Column(
        modifier            = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Summary tiles ─────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryTile("Teams",   d.teamCount.toString(),   Color(0xFF1565C0), Modifier.weight(1f))
            SummaryTile("Players", d.playerCount.toString(), Color(0xFF6A1B9A), Modifier.weight(1f))
            SummaryTile("Goals",   d.totalGoals.toString(),  GreenDark,         Modifier.weight(1f))
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryTile("Played",   d.finishedCount.toString(), Color(0xFF2E7D32), Modifier.weight(1f))
            SummaryTile("Upcoming", d.upcomingCount.toString(), Color(0xFFE65100), Modifier.weight(1f))
        }

        // ── Leaderboard ───────────────────────────────────────────────────────
        if (d.leaderboard.isNotEmpty()) {
            SectionTitle("Leaderboard")
            Card(
                elevation = CardDefaults.cardElevation(2.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // header row
                    Row(Modifier.fillMaxWidth()) {
                        Text("", Modifier.weight(0.4f))
                        Text("Team", Modifier.weight(2.5f),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        for (h in listOf("P","W","D","L","Pts")) {
                            Text(h, Modifier.weight(0.6f),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center)
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    d.leaderboard.forEach { s -> LeaderboardRow(s) }
                }
            }
        }

        // ── Top scorers ───────────────────────────────────────────────────────
        if (d.topScorers.isNotEmpty()) {
            SectionTitle("Top Scorers")
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                d.topScorers.forEachIndexed { idx, player ->
                    ScorerCard(rank = idx + 1, player = player, modifier = Modifier.weight(1f))
                }
            }
        }

        // ── Recent results ────────────────────────────────────────────────────
        if (d.recentResults.isNotEmpty()) {
            SectionTitle("Recent Results")
            d.recentResults.forEach { match -> ResultCard(match) }
        }

        // ── Next matches ──────────────────────────────────────────────────────
        if (d.nextMatches.isNotEmpty()) {
            SectionTitle("Upcoming Matches")
            d.nextMatches.forEach { match -> UpcomingCard(match) }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ── Small components ──────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color      = GreenDark
    )
}

@Composable
private fun SummaryTile(label: String, value: String, accentColor: Color, modifier: Modifier) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier  = modifier
    ) {
        Column(
            modifier            = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = value,
                fontSize   = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = accentColor
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LeaderboardRow(s: Standing) {
    val medalColor = when (s.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFBF8970)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank badge
        Box(
            modifier          = Modifier
                .weight(0.4f)
                .size(24.dp)
                .clip(CircleShape)
                .background(medalColor.copy(alpha = 0.15f)),
            contentAlignment  = Alignment.Center
        ) {
            Text(
                text       = s.rank.toString(),
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                color      = medalColor
            )
        }
        Text(
            text     = s.team.name,
            style    = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(2.5f)
        )
        for (v in listOf(s.played, s.won, s.drawn, s.lost, s.points)) {
            Text(
                text      = v.toString(),
                style     = MaterialTheme.typography.bodySmall,
                fontWeight = if (v == s.points) FontWeight.Bold else FontWeight.Normal,
                color     = if (v == s.points) GreenDark else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier  = Modifier.weight(0.6f)
            )
        }
    }
}

@Composable
private fun ScorerCard(rank: Int, player: Player, modifier: Modifier) {
    val accent = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFB0BEC5)
        else -> Color(0xFFBF8970)
    }
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier  = modifier
    ) {
        Column(
            modifier            = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier         = Modifier.size(32.dp).clip(CircleShape).background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("$rank", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accent)
            }
            Text(
                text      = player.name,
                style     = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text      = "${player.goals} goals",
                style     = MaterialTheme.typography.bodySmall,
                color     = GreenDark,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ResultCard(match: Match) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = match.homeTeam.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    textAlign  = TextAlign.End,
                    modifier   = Modifier.weight(1f)
                )
                Text(
                    text       = "  ${match.homeScore}  –  ${match.awayScore}  ",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = GreenDark
                )
                Text(
                    text       = match.awayTeam.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "${match.date}  •  ${match.location}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UpcomingCard(match: Match) {
    Card(
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date box
            Surface(
                shape = MaterialTheme.shapes.small,
                color = GreenPrimary.copy(alpha = 0.12f),
            ) {
                Column(
                    modifier            = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val parts = match.date.split("-")
                    Text(
                        text       = parts.getOrNull(2) ?: match.date,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = GreenDark
                    )
                    Text(
                        text  = when (parts.getOrNull(1)) {
                            "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"; "04" -> "Apr"
                            "05" -> "May"; "06" -> "Jun"; "07" -> "Jul"; "08" -> "Aug"
                            "09" -> "Sep"; "10" -> "Oct"; "11" -> "Nov"; "12" -> "Dec"
                            else -> parts.getOrNull(1) ?: ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenDark
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text       = "${match.homeTeam.name}  vs  ${match.awayTeam.name}",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text  = "${match.time}  •  ${match.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
