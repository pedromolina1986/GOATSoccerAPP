package com.goatsoccer.manager.data.repository

import com.goatsoccer.manager.data.api.RetrofitClient
import com.goatsoccer.manager.data.local.SessionManager
import com.goatsoccer.manager.data.model.Player
import com.goatsoccer.manager.data.model.Standing
import com.goatsoccer.manager.util.Resource

class StandingsRepository(private val sessionManager: SessionManager) {

    private val api = RetrofitClient.create(sessionManager)

    suspend fun getAllStandings(): Resource<List<Standing>> {
        return try {
            val response = api.getAllStandings()
            if (response.isSuccessful && response.body() != null)
                Resource.Success(response.body()!!.data)
            else Resource.Error(response.message() ?: "Failed to fetch standings")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getStandingsByLeague(leagueId: String): Resource<List<Standing>> {
        return try {
            val response = api.getStandingsByLeague(leagueId)
            if (response.isSuccessful && response.body() != null)
                Resource.Success(response.body()!!.data)
            else Resource.Error(response.message() ?: "Failed to fetch standings")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getTopScorers(leagueId: String? = null): Resource<List<Player>> {
        return try {
            val response = api.getTopScorers(leagueId)
            if (response.isSuccessful && response.body() != null)
                Resource.Success(response.body()!!.data)
            else Resource.Error(response.message() ?: "Failed to fetch top scorers")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
