package com.goatsoccer.manager.ui.teams

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goatsoccer.manager.data.model.Player
import com.goatsoccer.manager.data.model.PlayerLookupResult
import com.goatsoccer.manager.data.model.Team
import com.goatsoccer.manager.data.repository.TeamRepository
import com.goatsoccer.manager.util.Resource
import kotlinx.coroutines.launch

class TeamDetailViewModel(private val teamRepository: TeamRepository) : ViewModel() {

    private val _team = MutableLiveData<Resource<Team>>()
    val team: LiveData<Resource<Team>> = _team

    private val _players = MutableLiveData<Resource<List<Player>>>()
    val players: LiveData<Resource<List<Player>>> = _players

    private val _addPlayerResult = MutableLiveData<Resource<Player>>()
    val addPlayerResult: LiveData<Resource<Player>> = _addPlayerResult

    private val _actionResult = MutableLiveData<Resource<Unit>>()
    val actionResult: LiveData<Resource<Unit>> = _actionResult

    private val _lookupResult = MutableLiveData<Resource<PlayerLookupResult>>()
    val lookupResult: LiveData<Resource<PlayerLookupResult>> = _lookupResult

    private val _inviteResult = MutableLiveData<Resource<Player>>()
    val inviteResult: LiveData<Resource<Player>> = _inviteResult

    private var currentTeamId = ""

    fun loadTeam(teamId: String) {
        currentTeamId = teamId
        _team.value = Resource.Loading()
        viewModelScope.launch {
            _team.value = teamRepository.getTeamById(teamId)
        }
    }

    fun loadPlayers(teamId: String) {
        currentTeamId = teamId
        _players.value = Resource.Loading()
        viewModelScope.launch {
            _players.value = teamRepository.getPlayersByTeam(teamId)
        }
    }

    fun addPlayer(teamId: String, name: String, position: String, number: Int) {
        if (name.isBlank()) { _addPlayerResult.value = Resource.Error("Player name is required"); return }
        _addPlayerResult.value = Resource.Loading()
        viewModelScope.launch {
            val result = teamRepository.addPlayer(teamId, name, position, number)
            _addPlayerResult.value = result
            if (result is Resource.Success) loadPlayers(teamId)
        }
    }

    fun deletePlayer(teamId: String, playerId: String) {
        viewModelScope.launch {
            val result = teamRepository.deletePlayer(teamId, playerId)
            _actionResult.value = result
            if (result is Resource.Success) {
                // Optimistically remove from the current list — no loading flash
                val updated = (_players.value as? Resource.Success)?.data
                    ?.filter { it.id != playerId } ?: emptyList()
                _players.value = Resource.Success(updated)
            }
        }
    }

    fun lookupEmail(teamId: String, email: String) {
        if (email.isBlank()) { _lookupResult.value = Resource.Error("Enter an email address"); return }
        _lookupResult.value = Resource.Loading()
        viewModelScope.launch {
            _lookupResult.value = teamRepository.lookupPlayerByEmail(teamId, email)
        }
    }

    fun invitePlayer(teamId: String, email: String, name: String, position: String, number: Int) {
        _inviteResult.value = Resource.Loading()
        viewModelScope.launch {
            val result = teamRepository.invitePlayer(teamId, email, name, position, number)
            _inviteResult.value = result
            if (result is Resource.Success) loadPlayers(teamId)
        }
    }

    fun setCaptain(playerId: String) {
        viewModelScope.launch {
            val result = teamRepository.setCaptain(currentTeamId, playerId)
            _actionResult.value = result
            if (result is Resource.Success) loadPlayers(currentTeamId)
        }
    }
}
