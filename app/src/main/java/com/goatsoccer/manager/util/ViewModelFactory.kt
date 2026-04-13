package com.goatsoccer.manager.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.goatsoccer.manager.data.local.SessionManager
import com.goatsoccer.manager.data.repository.AuthRepository
import com.goatsoccer.manager.data.repository.MatchRepository
import com.goatsoccer.manager.data.repository.RoastRepository
import com.goatsoccer.manager.data.repository.StandingsRepository
import com.goatsoccer.manager.data.repository.TeamRepository
import com.goatsoccer.manager.data.repository.LeagueRepository
import com.goatsoccer.manager.ui.auth.LoginViewModel
import com.goatsoccer.manager.ui.auth.RegisterViewModel
import com.goatsoccer.manager.ui.home.HomeViewModel
import com.goatsoccer.manager.ui.leagues.LeaguesViewModel
import com.goatsoccer.manager.ui.profile.ProfileViewModel
import com.goatsoccer.manager.ui.matches.MatchesViewModel
import com.goatsoccer.manager.ui.roasts.RoastsViewModel
import com.goatsoccer.manager.ui.standings.StandingsViewModel
import com.goatsoccer.manager.ui.teams.TeamDetailViewModel
import com.goatsoccer.manager.ui.teams.TeamsViewModel

class ViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    private val authRepository by lazy { AuthRepository(sessionManager) }
    private val teamRepository by lazy { TeamRepository(sessionManager) }
    private val matchRepository by lazy { MatchRepository(sessionManager) }
    private val standingsRepository by lazy { StandingsRepository(sessionManager) }
    private val roastRepository by lazy { RoastRepository(sessionManager) }
    private val leagueRepository by lazy { LeagueRepository(sessionManager) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(teamRepository, matchRepository, standingsRepository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(sessionManager, authRepository) as T
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(authRepository) as T
            modelClass.isAssignableFrom(RegisterViewModel::class.java) ->
                RegisterViewModel(authRepository) as T
            modelClass.isAssignableFrom(TeamsViewModel::class.java) ->
                TeamsViewModel(teamRepository, leagueRepository) as T
            modelClass.isAssignableFrom(TeamDetailViewModel::class.java) ->
                TeamDetailViewModel(teamRepository) as T
            modelClass.isAssignableFrom(MatchesViewModel::class.java) ->
                MatchesViewModel(matchRepository, teamRepository, leagueRepository) as T
            modelClass.isAssignableFrom(LeaguesViewModel::class.java) ->
                LeaguesViewModel(leagueRepository, teamRepository) as T
            modelClass.isAssignableFrom(StandingsViewModel::class.java) ->
                StandingsViewModel(standingsRepository, leagueRepository) as T
            modelClass.isAssignableFrom(RoastsViewModel::class.java) ->
                RoastsViewModel(roastRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
