package com.goatsoccer.manager.data.repository

import com.goatsoccer.manager.data.api.RetrofitClient
import com.goatsoccer.manager.data.local.SessionManager
import com.goatsoccer.manager.data.model.CreatePlayerRequest
import com.goatsoccer.manager.data.model.CreateTeamRequest
import com.goatsoccer.manager.data.model.InvitePlayerRequest
import com.goatsoccer.manager.data.model.Player
import com.goatsoccer.manager.data.model.PlayerLookupResult
import com.goatsoccer.manager.data.model.Team
import com.goatsoccer.manager.util.Resource

class TeamRepository(private val sessionManager: SessionManager) {

    private val api = RetrofitClient.create(sessionManager)

    suspend fun getTeams(): Resource<List<Team>> {
        return try {
            val response = api.getTeams()
            if (response.isSuccessful && response.body() != null)
                Resource.Success(response.body()!!.data)
            else Resource.Error(response.message() ?: "Failed to fetch teams")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getTeamById(id: String): Resource<Team> {
        return try {
            val response = api.getTeamById(id)
            if (response.isSuccessful && response.body()?.data != null)
                Resource.Success(response.body()!!.data!!)
            else Resource.Error(response.message() ?: "Team not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun createTeam(name: String, city: String, coach: String): Resource<Team> {
        return try {
            val response = api.createTeam(CreateTeamRequest(name, city, coach))
            if (response.isSuccessful && response.body()?.data != null)
                Resource.Success(response.body()!!.data!!)
            else Resource.Error(response.message() ?: "Failed to create team")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateTeam(id: String, name: String, city: String, coach: String): Resource<Team> {
        return try {
            val response = api.updateTeam(id, CreateTeamRequest(name, city, coach))
            if (response.isSuccessful && response.body()?.data != null)
                Resource.Success(response.body()!!.data!!)
            else Resource.Error(response.message() ?: "Failed to update team")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun deleteTeam(id: String): Resource<Unit> {
        return try {
            val response = api.deleteTeam(id)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(response.message() ?: "Failed to delete team")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getPlayersByTeam(teamId: String): Resource<List<Player>> {
        return try {
            val response = api.getPlayersByTeam(teamId)
            if (response.isSuccessful && response.body() != null)
                Resource.Success(response.body()!!.data)
            else Resource.Error(response.message() ?: "Failed to fetch players")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun addPlayer(teamId: String, name: String, position: String, number: Int): Resource<Player> {
        return try {
            val response = api.addPlayer(teamId, CreatePlayerRequest(name, position, number))
            if (response.isSuccessful && response.body()?.data != null)
                Resource.Success(response.body()!!.data!!)
            else Resource.Error(response.message() ?: "Failed to add player")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun deletePlayer(teamId: String, playerId: String): Resource<Unit> {
        return try {
            val response = api.deletePlayer(teamId, playerId)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(response.message() ?: "Failed to delete player")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun lookupPlayerByEmail(teamId: String, email: String): Resource<PlayerLookupResult> {
        return try {
            val response = api.lookupPlayerByEmail(teamId, email)
            if (response.isSuccessful && response.body()?.data != null)
                Resource.Success(response.body()!!.data!!)
            else Resource.Error(response.message() ?: "Lookup failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun invitePlayer(
        teamId: String, email: String,
        name: String, position: String, number: Int
    ): Resource<Player> {
        return try {
            val response = api.invitePlayer(teamId, InvitePlayerRequest(email, name, position, number))
            if (response.isSuccessful && response.body()?.data != null)
                Resource.Success(response.body()!!.data!!)
            else Resource.Error(response.message() ?: "Invite failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun setCaptain(teamId: String, playerId: String): Resource<Unit> {
        return try {
            val response = api.setCaptain(teamId, playerId)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(response.message() ?: "Failed to set captain")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
