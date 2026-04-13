package com.goatsoccer.manager.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goatsoccer.manager.data.model.Match
import com.goatsoccer.manager.data.model.Player
import com.goatsoccer.manager.data.model.Standing
import com.goatsoccer.manager.data.repository.MatchRepository
import com.goatsoccer.manager.data.repository.StandingsRepository
import com.goatsoccer.manager.data.repository.TeamRepository
import com.goatsoccer.manager.util.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class DashboardData(
    val teamCount: Int,
    val playerCount: Int,
    val finishedCount: Int,
    val upcomingCount: Int,
    val totalGoals: Int,
    val leaderboard: List<Standing>,       // top 3 global
    val topScorers: List<Player>,          // top 3
    val recentResults: List<Match>,        // last 3 finished
    val nextMatches: List<Match>,          // next 3 scheduled
)

class HomeViewModel(
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository,
    private val standingsRepository: StandingsRepository
) : ViewModel() {

    private val _dashboard = MutableLiveData<Resource<DashboardData>>()
    val dashboard: LiveData<Resource<DashboardData>> = _dashboard

    init { loadDashboard() }

    fun loadDashboard() {
        _dashboard.value = Resource.Loading()
        doLoad()
    }

    fun silentRefresh() = doLoad()

    private fun doLoad() {
        viewModelScope.launch {
            val teamsDeferred     = async { teamRepository.getTeams() }
            val matchesDeferred   = async { matchRepository.getMatches() }
            val standingsDeferred = async { standingsRepository.getAllStandings() }
            val scorersDeferred   = async { standingsRepository.getTopScorers() }

            val teams     = (teamsDeferred.await()     as? Resource.Success)?.data ?: emptyList()
            val matches   = (matchesDeferred.await()   as? Resource.Success)?.data ?: emptyList()
            val standings = (standingsDeferred.await() as? Resource.Success)?.data ?: emptyList()
            val scorers   = (scorersDeferred.await()   as? Resource.Success)?.data ?: emptyList()

            val finished  = matches.filter { it.status == "finished" }
            val upcoming  = matches.filter { it.status == "scheduled" }.sortedBy { it.date }

            _dashboard.value = Resource.Success(
                DashboardData(
                    teamCount     = teams.size,
                    playerCount   = teams.sumOf { it.playerCount },
                    finishedCount = finished.size,
                    upcomingCount = upcoming.size,
                    totalGoals    = finished.sumOf { it.homeScore + it.awayScore },
                    leaderboard   = standings.sortedBy { it.rank }.take(3),
                    topScorers    = scorers.take(3),
                    recentResults = finished.sortedByDescending { it.date }.take(3),
                    nextMatches   = upcoming.take(3),
                )
            )
        }
    }

    // kept for backwards compat
    val stats get() = _dashboard
    val latestResult get() = _dashboard
    val upcomingMatches get() = _dashboard
}
