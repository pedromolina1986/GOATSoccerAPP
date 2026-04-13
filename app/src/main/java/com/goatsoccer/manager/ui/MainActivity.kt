package com.goatsoccer.manager.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.goatsoccer.manager.R
import com.goatsoccer.manager.data.local.SessionManager
import com.goatsoccer.manager.ui.auth.LoginActivity
import com.goatsoccer.manager.ui.home.HomeScreen
import com.goatsoccer.manager.ui.leagues.LeaguesScreen
import com.goatsoccer.manager.ui.matches.MatchesScreen
import com.goatsoccer.manager.ui.profile.ProfileScreen
import com.goatsoccer.manager.ui.roasts.RoastsScreen
import com.goatsoccer.manager.ui.standings.StandingsScreen
import com.goatsoccer.manager.ui.teams.TeamDetailScreen
import com.goatsoccer.manager.ui.teams.TeamsScreen
import com.goatsoccer.manager.ui.teams.MyTeamScreen
import com.goatsoccer.manager.ui.theme.GoatSoccerTheme

// ── Navigation destinations ───────────────────────────────────────────────────

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home      : Screen("home",      "Home",      Icons.Default.Home)
    object Teams     : Screen("teams",     "Teams",     Icons.Default.Groups)
    object MyTeam    : Screen("myTeam",    "My Team",   Icons.Default.Group)
    object Matches   : Screen("matches",   "Matches",   Icons.Default.SportsSoccer)
    object Standings : Screen("standings", "Standings", Icons.Default.Leaderboard)
    object Leagues   : Screen("leagues",   "Leagues",   Icons.Default.EmojiEvents)
    object Roasts    : Screen("roasts",    "Roasts",    Icons.Default.Forum)
    object Profile   : Screen("profile",   "Profile",   Icons.Default.Person)
    object TeamDetail: Screen("teamDetail/{teamId}", "Team", Icons.Default.Group) {
        fun withId(id: String) = "teamDetail/$id"
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Crash reporter ────────────────────────────────────────────────────
        // If the previous run crashed, show the error as a Toast so we can
        // diagnose it without needing Logcat.
        val prefs = getSharedPreferences("crash_report", Context.MODE_PRIVATE)
        prefs.getString("last_crash", null)?.let { msg ->
            prefs.edit().remove("last_crash").apply()
            Toast.makeText(this, "CRASH: $msg", Toast.LENGTH_LONG).show()
            Log.e("CrashReporter", "Previous crash: $msg")
        }
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val trace = throwable.stackTrace.take(6).joinToString(" | ") {
                "${it.className.substringAfterLast('.')}:${it.lineNumber}"
            }
            val msg = "${throwable::class.simpleName}: ${throwable.message} — $trace"
            Log.e("CrashReporter", "Uncaught exception on ${thread.name}", throwable)
            prefs.edit().putString("last_crash", msg).commit() // commit = synchronous
            defaultHandler?.uncaughtException(thread, throwable)
        }
        // ─────────────────────────────────────────────────────────────────────

        val session = SessionManager(this)
        val role    = session.getUserRole() ?: "fan"

        setContent {
            GoatSoccerTheme {
                MainApp(
                    role     = role,
                    session  = session,
                    onLogout = {
                        session.clearSession()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finishAffinity()
                    }
                )
            }
        }
    }
}

// ── Root composable ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(role: String, onLogout: () -> Unit, session: SessionManager? = null) {
    val navController = rememberNavController()

    // Role-based bottom nav tabs
    val bottomTabs: List<Screen> = when (role) {
        "coach"  -> listOf(Screen.Home, Screen.Teams, Screen.Matches, Screen.Standings, Screen.Leagues)
        "player" -> listOf(Screen.Home, Screen.MyTeam, Screen.Matches, Screen.Standings)
        else     -> listOf(Screen.Home, Screen.Matches, Screen.Standings, Screen.Roasts)
    }

    val allTabs = bottomTabs + Screen.Profile

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentTitle = allTabs
        .firstOrNull { it.route == currentDestination?.route }
        ?.label ?: "Goat Soccer"

    val userInitial = (session?.getUserName() ?: "?")
        .firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    val isOnProfile = currentDestination?.route == Screen.Profile.route

    fun navigateToProfile() {
        navController.navigate(Screen.Profile.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.goat_logo),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(currentTitle)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOnProfile) Color.White.copy(alpha = 0.35f)
                                else Color.White.copy(alpha = 0.20f)
                            )
                            .clickable { navigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = userInitial,
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomTabs.forEach { screen ->
                    NavigationBarItem(
                        icon     = { Icon(screen.icon, contentDescription = screen.label) },
                        label    = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick  = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route)      { HomeScreen() }
            composable(Screen.Teams.route)     {
                TeamsScreen(onNavigateToDetail = { teamId ->
                    navController.navigate(Screen.TeamDetail.withId(teamId))
                })
            }
            composable(
                route = Screen.TeamDetail.route,
                arguments = listOf(navArgument("teamId") { type = NavType.StringType })
            ) { backStack ->
                val teamId = backStack.arguments?.getString("teamId") ?: ""
                TeamDetailScreen(teamId = teamId, isCoach = role == "coach")
            }
            composable(Screen.MyTeam.route)    { MyTeamScreen() }
            composable(Screen.Matches.route)   { MatchesScreen(isCoach = role == "coach") }
            composable(Screen.Standings.route) { StandingsScreen() }
            composable(Screen.Leagues.route)   { LeaguesScreen(isCoach = role == "coach") }
            composable(Screen.Roasts.route)    { RoastsScreen() }
            composable(Screen.Profile.route)   { ProfileScreen(onLogout = onLogout) }
        }
    }
}
