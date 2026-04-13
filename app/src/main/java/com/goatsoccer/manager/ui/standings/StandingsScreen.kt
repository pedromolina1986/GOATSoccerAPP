package com.goatsoccer.manager.ui.standings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goatsoccer.manager.data.model.Player
import com.goatsoccer.manager.data.model.Standing
import com.goatsoccer.manager.ui.matches.LeagueChipRow
import com.goatsoccer.manager.ui.theme.GreenDark
import com.goatsoccer.manager.ui.theme.GreenPrimary
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.goatsoccer.manager.util.Resource
import com.goatsoccer.manager.util.appViewModel
import com.goatsoccer.manager.data.model.League

sealed class StandingsItem {
    data class Header(val league: League) : StandingsItem()
    data class Row(val standing: Standing) : StandingsItem()
}

@Composable
fun StandingsScreen(viewModel: StandingsViewModel = appViewModel()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.silentRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val groupedItemsState by viewModel.groupedItems.observeAsState()
    val topScorersState   by viewModel.topScorers.observeAsState()
    val leagues           by viewModel.leagues.observeAsState(emptyList())
    val selectedLeagueId  by viewModel.selectedLeagueId.observeAsState()
    val activeTab         by viewModel.activeTab.observeAsState("table")

    Column(Modifier.fillMaxSize()) {

        // League filter chips
        if (leagues.isNotEmpty()) {
            LeagueChipRow(
                leagues          = leagues,
                selectedLeagueId = selectedLeagueId,
                onSelect         = { viewModel.selectLeague(it) }
            )
        }

        // Tab row
        TabRow(
            selectedTabIndex = if (activeTab == "table") 0 else 1,
            containerColor   = MaterialTheme.colorScheme.surface,
            contentColor     = GreenPrimary,
            indicator        = { tabPositions ->
                val idx = if (activeTab == "table") 0 else 1
                TabRowDefaults.Indicator(
                    modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.BottomStart)
                        .offset(x = tabPositions[idx].left)
                        .width(tabPositions[idx].width),
                    color    = GreenPrimary,
                    height   = 3.dp
                )
            }
        ) {
            Tab(
                selected = activeTab == "table",
                onClick  = { viewModel.setActiveTab("table") },
                text     = { Text("League Table", fontWeight = if (activeTab == "table") FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = activeTab == "scorers",
                onClick  = { viewModel.setActiveTab("scorers") },
                text     = { Text("Top Scorers", fontWeight = if (activeTab == "scorers") FontWeight.Bold else FontWeight.Normal) }
            )
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (activeTab) {
                "table"   -> StandingsTableContent(groupedItemsState)
                "scorers" -> TopScorersContent(topScorersState)
            }
        }
    }
}

// ── League Table ──────────────────────────────────────────────────────────────

@Composable
private fun StandingsTableContent(state: Resource<List<StandingsItem>>?) {
    when {
        state == null || state is Resource.Loading ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GreenPrimary) }
        state is Resource.Error ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message ?: "Error", color = MaterialTheme.colorScheme.error)
            }
        state is Resource.Success -> {
            val items = state.data ?: emptyList()
            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No standings available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                    itemsIndexed(items, key = { _, it ->
                        when (it) {
                            is StandingsItem.Header -> "h_${it.league.id}"
                            is StandingsItem.Row    -> "r_${it.standing.id}"
                        }
                    }) { _, item ->
                        when (item) {
                            is StandingsItem.Header -> LeagueHeaderBlock(item)
                            is StandingsItem.Row    -> StandingRow(item.standing)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeagueHeaderBlock(item: StandingsItem.Header) {
    Column {
        // League name + season
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GreenDark, GreenPrimary)))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = item.league.name,
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.titleSmall
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color.White.copy(alpha = 0.20f)
                ) {
                    Text(
                        text     = item.league.season,
                        color    = Color.White,
                        style    = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
        // Column headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#",    style = colHeaderStyle(), modifier = Modifier.width(28.dp))
            Text("Team", style = colHeaderStyle(), modifier = Modifier.weight(1f))
            listOf("P", "W", "D", "L", "GF", "GA", "GD", "Pts").forEach { col ->
                Text(
                    text      = col,
                    style     = colHeaderStyle(),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.width(colWidth(col))
                )
            }
        }
    }
}

@Composable
private fun StandingRow(s: Standing) {
    val medalColor = when (s.rank) {
        1    -> Color(0xFFFFD700)
        2    -> Color(0xFFB0BEC5)
        3    -> Color(0xFFBF8970)
        else -> null
    }
    val gdText = if (s.goalDifference >= 0) "+${s.goalDifference}" else s.goalDifference.toString()
    val gdColor = when {
        s.goalDifference > 0 -> Color(0xFF2E7D32)
        s.goalDifference < 0 -> Color(0xFFC62828)
        else                  -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (s.rank % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank badge
        if (medalColor != null) {
            Box(
                modifier         = Modifier.width(28.dp).size(22.dp).clip(CircleShape).background(medalColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(s.rank.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = medalColor)
            }
        } else {
            Text(
                s.rank.toString(),
                fontSize  = 12.sp,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier  = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )
        }

        Text(
            text       = s.team.name,
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            modifier   = Modifier.weight(1f)
        )

        listOf(
            s.played.toString() to null,
            s.won.toString()    to Color(0xFF2E7D32),
            s.drawn.toString()  to Color(0xFF1565C0),
            s.lost.toString()   to Color(0xFFC62828),
            s.goalsFor.toString()     to null,
            s.goalsAgainst.toString() to null,
        ).forEach { (value, color) ->
            Text(
                text      = value,
                fontSize  = 12.sp,
                color     = color ?: MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier  = Modifier.width(colWidth(value))
            )
        }

        // GD
        Text(
            text       = gdText,
            fontSize   = 12.sp,
            color      = gdColor,
            fontWeight = FontWeight.Medium,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.width(colWidth("GD"))
        )

        // Pts — highlighted
        Text(
            text       = s.points.toString(),
            fontSize   = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = GreenDark,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.width(colWidth("Pts"))
        )
    }
}

// ── Top Scorers ───────────────────────────────────────────────────────────────

@Composable
private fun TopScorersContent(state: Resource<List<Player>>?) {
    when {
        state == null || state is Resource.Loading ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GreenPrimary) }
        state is Resource.Error ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message ?: "Error", color = MaterialTheme.colorScheme.error)
            }
        state is Resource.Success -> {
            val scorers = (state.data ?: emptyList()).filter { it.goals > 0 }
            if (scorers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No goals scored yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)) {
                    // Podium for top 3
                    if (scorers.size >= 3) {
                        item {
                            Podium(scorers[0], scorers[1], scorers.getOrNull(2))
                            Spacer(Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    // Rest of scorers
                    val rest = if (scorers.size >= 3) scorers.drop(3) else scorers
                    itemsIndexed(rest, key = { _, p -> p.id }) { idx, player ->
                        ScorerListRow(rank = idx + 4, player = player)
                        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun Podium(gold: Player, silver: Player, bronze: Player?) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.Bottom
    ) {
        PodiumCard(rank = 2, player = silver, heightFraction = 0.80f, color = Color(0xFFB0BEC5))
        PodiumCard(rank = 1, player = gold,   heightFraction = 1.00f, color = Color(0xFFFFD700))
        if (bronze != null)
            PodiumCard(rank = 3, player = bronze, heightFraction = 0.65f, color = Color(0xFFBF8970))
    }
}

@Composable
private fun PodiumCard(rank: Int, player: Player, heightFraction: Float, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.width(100.dp)
    ) {
        // Avatar
        Box(
            modifier         = Modifier.size(52.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = player.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = color
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text      = player.name,
            fontSize  = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text      = player.teamName.ifBlank { "" },
            fontSize  = 10.sp,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        // Podium block
        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .height((72.dp.value * heightFraction).dp)
                .clip(MaterialTheme.shapes.small)
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(rank.toString(), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
                Text("${player.goals} goals", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
private fun ScorerListRow(rank: Int, player: Player) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = rank.toString(),
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier   = Modifier.width(28.dp),
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.width(10.dp))
        // Mini avatar
        Box(
            modifier         = Modifier.size(32.dp).clip(CircleShape).background(GreenPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = player.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = GreenPrimary
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(player.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            if (player.teamName.isNotBlank()) {
                Text(player.teamName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            text       = "${player.goals}",
            fontSize   = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = GreenDark
        )
        Text(
            text     = " goals",
            fontSize = 11.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 2.dp).align(Alignment.Bottom)
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun colHeaderStyle() = MaterialTheme.typography.labelSmall.copy(
    color      = MaterialTheme.colorScheme.onSurfaceVariant,
    fontWeight = FontWeight.SemiBold
)

private fun colWidth(col: String) = when (col) {
    "Pts"      -> 32.dp
    "GD"       -> 32.dp
    "GF", "GA" -> 28.dp
    else       -> 24.dp
}
