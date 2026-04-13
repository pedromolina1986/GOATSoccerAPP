package com.goatsoccer.manager.ui.matches

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goatsoccer.manager.data.model.GoalEvent
import com.goatsoccer.manager.data.model.League
import com.goatsoccer.manager.data.model.Match
import com.goatsoccer.manager.data.model.Player
import com.goatsoccer.manager.data.model.Team
import com.goatsoccer.manager.data.repository.LeagueRepository
import com.goatsoccer.manager.data.repository.MatchRepository
import com.goatsoccer.manager.data.repository.TeamRepository
import com.goatsoccer.manager.util.Resource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

private const val TAG = "MatchesViewModel"

class MatchesViewModel(
    private val matchRepository: MatchRepository,
    private val teamRepository: TeamRepository,
    private val leagueRepository: LeagueRepository
) : ViewModel() {

    private val crashHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Unhandled coroutine exception", throwable)
    }

    private val _matches = MutableLiveData<Resource<List<Match>>>()
    val matches: LiveData<Resource<List<Match>>> = _matches

    private val _teams = MutableLiveData<List<Team>>()
    val teams: LiveData<List<Team>> = _teams

    private val _leagues = MutableLiveData<List<League>>()
    val leagues: LiveData<List<League>> = _leagues

    private val _allPlayers = MutableLiveData<List<Player>>()
    val allPlayers: LiveData<List<Player>> = _allPlayers

    private val _createResult = MutableLiveData<Resource<Match>>()
    val createResult: LiveData<Resource<Match>> = _createResult

    private val _updateResult = MutableLiveData<Resource<Match>>()
    val updateResult: LiveData<Resource<Match>> = _updateResult

    private val _selectedLeagueId = MutableLiveData<String?>(null)
    val selectedLeagueId: LiveData<String?> = _selectedLeagueId

    private val _selectedTeamId = MutableLiveData<String?>(null)
    val selectedTeamId: LiveData<String?> = _selectedTeamId

    fun selectLeague(id: String?) {
        _selectedLeagueId.value = id
        _selectedTeamId.value = null  // reset team filter when league changes
    }

    fun selectTeam(id: String?) {
        _selectedTeamId.value = id
    }

    init {
        Log.d(TAG, "ViewModel created, starting data load")
        fetchMatches()
        loadTeams()
        loadLeagues()
        loadAllPlayers()
    }

    fun fetchMatches() {
        Log.d(TAG, "fetchMatches() called")
        _matches.value = Resource.Loading()
        doFetchMatches()
    }

    fun silentRefresh() {
        doFetchMatches()
        loadTeams()
        loadLeagues()
        loadAllPlayers()
    }

    private fun doFetchMatches() {
        viewModelScope.launch(crashHandler) {
            Log.d(TAG, "fetchMatches: calling API")
            val result = matchRepository.getMatches()
            Log.d(TAG, "fetchMatches: result = ${result::class.simpleName}, " +
                "data size = ${(result as? Resource.Success)?.data?.size ?: 0}, " +
                "error = ${(result as? Resource.Error)?.message}")
            _matches.value = result
        }
    }

    fun loadTeams() {
        viewModelScope.launch(crashHandler) {
            Log.d(TAG, "loadTeams: calling API")
            val result = teamRepository.getTeams()
            Log.d(TAG, "loadTeams: result = ${result::class.simpleName}, " +
                "count = ${(result as? Resource.Success)?.data?.size ?: 0}")
            if (result is Resource.Success) {
                val teams = result.data ?: emptyList()
                _teams.value = teams
                // Load all players using already-fetched team list (avoids extra getTeams call)
                val combined = teams.flatMap { team ->
                    val r = teamRepository.getPlayersByTeam(team.id)
                    if (r is Resource.Success) r.data ?: emptyList() else emptyList()
                }
                Log.d(TAG, "loadTeams: total players loaded = ${combined.size}")
                _allPlayers.value = combined
            }
        }
    }

    private fun loadAllPlayers() {
        // Re-uses already-loaded teams to avoid a redundant getTeams() call
        viewModelScope.launch(crashHandler) {
            val teams = _teams.value ?: emptyList()
            if (teams.isEmpty()) return@launch
            val combined = teams.flatMap { team ->
                val r = teamRepository.getPlayersByTeam(team.id)
                if (r is Resource.Success) r.data ?: emptyList() else emptyList()
            }
            Log.d(TAG, "loadAllPlayers: total players = ${combined.size}")
            _allPlayers.value = combined
        }
    }

    fun loadLeagues() {
        viewModelScope.launch(crashHandler) {
            Log.d(TAG, "loadLeagues: calling API")
            val result = leagueRepository.getLeagues()
            Log.d(TAG, "loadLeagues: result = ${result::class.simpleName}, " +
                "count = ${(result as? Resource.Success)?.data?.size ?: 0}")
            if (result is Resource.Success) _leagues.value = result.data ?: emptyList()
        }
    }

    fun createMatch(
        homeTeamId: String, awayTeamId: String,
        date: String, time: String, location: String, leagueId: String
    ) {
        _createResult.value = Resource.Loading()
        viewModelScope.launch {
            val result = matchRepository.createMatch(homeTeamId, awayTeamId, date, time, location, leagueId)
            _createResult.value = result
            if (result is Resource.Success) fetchMatches()
        }
    }

    fun updateScore(
        matchId: String, homeScore: Int, awayScore: Int,
        status: String, scorers: List<GoalEvent> = emptyList()
    ) {
        viewModelScope.launch {
            val result = matchRepository.updateScore(matchId, homeScore, awayScore, status, scorers)
            _updateResult.value = result
            if (result is Resource.Success) fetchMatches()
        }
    }

    fun addGoal(matchId: String, goal: GoalEvent) {
        val matches = (_matches.value as? Resource.Success)?.data ?: return
        val match = matches.find { it.id == matchId } ?: return
        val newScorers = match.goalScorers + goal
        val homeScore = newScorers.count { it.teamId == match.homeTeam.id }
        val awayScore = newScorers.count { it.teamId == match.awayTeam.id }
        // Keep current status — goals don't change the match state
        updateScore(matchId, homeScore, awayScore, match.status, newScorers)
    }

    fun removeGoal(matchId: String, goalIndex: Int) {
        val matches = (_matches.value as? Resource.Success)?.data ?: return
        val match = matches.find { it.id == matchId } ?: return
        if (goalIndex !in match.goalScorers.indices) return
        val newScorers = match.goalScorers.toMutableList().also { it.removeAt(goalIndex) }
        val homeScore = newScorers.count { it.teamId == match.homeTeam.id }
        val awayScore = newScorers.count { it.teamId == match.awayTeam.id }
        // Keep current status — removing a goal doesn't change the match state
        updateScore(matchId, homeScore, awayScore, match.status, newScorers)
    }

    fun setMatchStatus(matchId: String, status: String) {
        val matches = (_matches.value as? Resource.Success)?.data ?: return
        val match = matches.find { it.id == matchId } ?: return
        updateScore(matchId, match.homeScore, match.awayScore, status, match.goalScorers)
    }

    fun deleteMatch(id: String) {
        viewModelScope.launch {
            matchRepository.deleteMatch(id)
            fetchMatches()
        }
    }
}
