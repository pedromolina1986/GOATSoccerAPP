package com.goatsoccer.manager.ui.standings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goatsoccer.manager.data.model.League
import com.goatsoccer.manager.data.model.Player
import com.goatsoccer.manager.data.repository.LeagueRepository
import com.goatsoccer.manager.data.repository.StandingsRepository
import com.goatsoccer.manager.ui.standings.StandingsItem
import com.goatsoccer.manager.util.Resource
import kotlinx.coroutines.launch

class StandingsViewModel(
    private val standingsRepository: StandingsRepository,
    private val leagueRepository: LeagueRepository
) : ViewModel() {

    // All items unfiltered — used to build filtered views
    private var allItems: List<StandingsItem> = emptyList()

    private val _groupedItems = MutableLiveData<Resource<List<StandingsItem>>>()
    val groupedItems: LiveData<Resource<List<StandingsItem>>> = _groupedItems

    private val _leagues = MutableLiveData<List<League>>()
    val leagues: LiveData<List<League>> = _leagues

    private val _topScorers = MutableLiveData<Resource<List<Player>>>()
    val topScorers: LiveData<Resource<List<Player>>> = _topScorers

    private val _selectedLeagueId = MutableLiveData<String?>(null)
    val selectedLeagueId: LiveData<String?> = _selectedLeagueId

    private val _activeTab = MutableLiveData("table")
    val activeTab: LiveData<String> = _activeTab

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    init {
        fetchStandings()
        fetchTopScorers()
    }

    fun fetchStandings() {
        _groupedItems.value = Resource.Loading()
        doFetchStandings()
    }

    fun silentRefresh() {
        doFetchStandings()
        fetchTopScorers(_selectedLeagueId.value)
    }

    private fun doFetchStandings() {
        viewModelScope.launch {
            val leaguesResult = leagueRepository.getLeagues()
            val leagues = (leaguesResult as? Resource.Success)?.data ?: emptyList()
            _leagues.value = leagues

            val items = mutableListOf<StandingsItem>()
            for (league in leagues) {
                val standingsResult = standingsRepository.getStandingsByLeague(league.id)
                val standings = (standingsResult as? Resource.Success)?.data ?: emptyList()
                if (standings.isNotEmpty()) {
                    items.add(StandingsItem.Header(league))
                    standings.sortedBy { it.rank }.forEach { items.add(StandingsItem.Row(it)) }
                }
            }
            allItems = items
            // Apply current filter when refreshing silently
            val filtered = if (_selectedLeagueId.value == null) items else {
                var inLeague = false
                items.filter { item ->
                    when (item) {
                        is StandingsItem.Header -> { inLeague = item.league.id == _selectedLeagueId.value; inLeague }
                        is StandingsItem.Row    -> inLeague
                    }
                }
            }
            _groupedItems.value = Resource.Success(filtered)
        }
    }

    /** Called when a league chip is tapped. Filters both tables and top scorers. */
    fun selectLeague(leagueId: String?) {
        _selectedLeagueId.value = leagueId
        // Filter league table
        if (leagueId == null) {
            _groupedItems.value = Resource.Success(allItems)
        } else {
            var inLeague = false
            val filtered = allItems.filter { item ->
                when (item) {
                    is StandingsItem.Header -> { inLeague = item.league.id == leagueId; inLeague }
                    is StandingsItem.Row -> inLeague
                }
            }
            _groupedItems.value = Resource.Success(filtered)
        }
        // Reload top scorers for the selected league
        fetchTopScorers(leagueId)
    }

    fun fetchTopScorers(leagueId: String? = null) {
        _topScorers.value = Resource.Loading()
        viewModelScope.launch {
            _topScorers.value = standingsRepository.getTopScorers(leagueId)
        }
    }
}
