package com.goatsoccer.manager.ui.teams

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

class TeamsViewModel(
    private val teamRepository: TeamRepository,
    private val leagueRepository: LeagueRepository
) : ViewModel() {

    private val _teams = MutableLiveData<Resource<List<Team>>>()
    val teams: LiveData<Resource<List<Team>>> = _teams

    private val _leagues = MutableLiveData<List<League>>(emptyList())
    val leagues: LiveData<List<League>> = _leagues

    private val _createResult = MutableLiveData<Resource<Team>>()
    val createResult: LiveData<Resource<Team>> = _createResult

    private val _deleteResult = MutableLiveData<Resource<Unit>>()
    val deleteResult: LiveData<Resource<Unit>> = _deleteResult

    init {
        fetchTeams()
        fetchLeagues()
    }

    fun fetchTeams() {
        _teams.value = Resource.Loading()
        doFetchTeams()
    }

    fun silentRefresh() {
        doFetchTeams()
        fetchLeagues()
    }

    private fun doFetchTeams() {
        viewModelScope.launch {
            _teams.value = teamRepository.getTeams()
        }
    }

    private fun fetchLeagues() {
        viewModelScope.launch {
            val result = leagueRepository.getLeagues()
            if (result is Resource.Success) _leagues.value = result.data ?: emptyList()
        }
    }

    fun createTeam(name: String, city: String, coach: String) {
        if (name.isBlank()) { _createResult.value = Resource.Error("Team name is required"); return }
        _createResult.value = Resource.Loading()
        viewModelScope.launch {
            _createResult.value = teamRepository.createTeam(name, city, coach)
        }
    }

    fun deleteTeam(id: String) {
        viewModelScope.launch {
            _deleteResult.value = teamRepository.deleteTeam(id)
        }
    }
}
