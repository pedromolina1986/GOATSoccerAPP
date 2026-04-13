package com.goatsoccer.manager.data.repository

import com.goatsoccer.manager.data.api.RetrofitClient
import com.goatsoccer.manager.data.local.SessionManager
import com.goatsoccer.manager.data.model.AddTeamToLeagueRequest
import com.goatsoccer.manager.data.model.CreateLeagueRequest
import com.goatsoccer.manager.data.model.League
import com.goatsoccer.manager.util.Resource

class LeagueRepository(private val sessionManager: SessionManager) {

    private val api = RetrofitClient.create(sessionManager)

    suspend fun getLeagues(): Resource<List<League>> {
        return try {
            val response = api.getLeagues()
            if (response.isSuccessful && response.body() != null)
                Resource.Success(response.body()!!.data)
            else Resource.Error(response.message() ?: "Failed to fetch leagues")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun createLeague(name: String, season: String): Resource<League> {
        return try {
            val response = api.createLeague(CreateLeagueRequest(name, season))
            if (response.isSuccessful && response.body() != null)
                Resource.Success(response.body()!!.data!!)
            else Resource.Error(response.message() ?: "Failed to create league")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun deleteLeague(id: String): Resource<Unit> {
        return try {
            val response = api.deleteLeague(id)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(response.message() ?: "Failed to delete league")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun addTeamToLeague(leagueId: String, teamId: String): Resource<League> {
        return try {
            val response = api.addTeamToLeague(leagueId, AddTeamToLeagueRequest(teamId))
            if (response.isSuccessful && response.body() != null)
                Resource.Success(response.body()!!.data!!)
            else Resource.Error(response.message() ?: "Failed to add team")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun removeTeamFromLeague(leagueId: String, teamId: String): Resource<League> {
        return try {
            val response = api.removeTeamFromLeague(leagueId, teamId)
            if (response.isSuccessful && response.body() != null)
                Resource.Success(response.body()!!.data!!)
            else Resource.Error(response.message() ?: "Failed to remove team")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
