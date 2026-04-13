package com.goatsoccer.manager.ui.leagues

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goatsoccer.manager.data.model.League
import com.goatsoccer.manager.data.model.Team
import com.goatsoccer.manager.data.repository.LeagueRepository
import com.goatsoccer.manager.data.repository.TeamRepository
import com.goatsoccer.manager.util.Resource
import kotlinx.coroutines.launch

class LeaguesViewModel(
    private val leagueRepository: LeagueRepository,
    private val teamRepository: TeamRepository
) : ViewModel() {

    private val _leagues = MutableLiveData<Resource<List<League>>>()
    val leagues: LiveData<Resource<List<League>>> = _leagues

    private val _teams = MutableLiveData<List<Team>>()
    val teams: LiveData<List<Team>> = _teams

    private val _actionResult = MutableLiveData<Resource<Any>>()
    val actionResult: LiveData<Resource<Any>> = _actionResult

    init {
        fetchLeagues()
        loadTeams()
    }

    fun fetchLeagues() {
        _leagues.value = Resource.Loading()
        doFetchLeagues()
    }

    fun silentRefresh() {
        doFetchLeagues()
        loadTeams()
    }

    private fun doFetchLeagues() {
        viewModelScope.launch {
            _leagues.value = leagueRepository.getLeagues()
        }
    }

    private fun loadTeams() {
        viewModelScope.launch {
            val result = teamRepository.getTeams()
            if (result is Resource.Success) _teams.value = result.data ?: emptyList()
        }
    }

    fun createLeague(name: String, season: String) {
        if (name.isBlank()) { _actionResult.value = Resource.Error("League name required"); return }
        viewModelScope.launch {
            val result = leagueRepository.createLeague(name, season)
            _actionResult.value = result as Resource<Any>
            if (result is Resource.Success) fetchLeagues()
        }
    }

    fun deleteLeague(id: String) {
        viewModelScope.launch {
            val result = leagueRepository.deleteLeague(id)
            _actionResult.value = result as Resource<Any>
            if (result is Resource.Success) fetchLeagues()
        }
    }

    fun addTeam(leagueId: String, teamId: String) {
        viewModelScope.launch {
            val result = leagueRepository.addTeamToLeague(leagueId, teamId)
            _actionResult.value = result as Resource<Any>
            if (result is Resource.Success) fetchLeagues()
        }
    }

    fun removeTeam(leagueId: String, teamId: String) {
        viewModelScope.launch {
            val result = leagueRepository.removeTeamFromLeague(leagueId, teamId)
            _actionResult.value = result as Resource<Any>
            if (result is Resource.Success) fetchLeagues()
        }
    }

    fun teamNameById(id: String): String =
        _teams.value?.find { it.id == id }?.name ?: id
}
