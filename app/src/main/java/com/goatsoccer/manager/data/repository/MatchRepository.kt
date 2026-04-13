package com.goatsoccer.manager.data.repository

import android.util.Log
import com.goatsoccer.manager.data.api.RetrofitClient
import com.goatsoccer.manager.data.local.SessionManager
import com.goatsoccer.manager.data.model.CreateMatchRequest
import com.goatsoccer.manager.data.model.GoalEvent
import com.goatsoccer.manager.data.model.Match
import com.goatsoccer.manager.data.model.UpdateScoreRequest
import com.goatsoccer.manager.util.Resource

private const val TAG = "MatchRepository"

class MatchRepository(private val sessionManager: SessionManager) {

    private val api = RetrofitClient.create(sessionManager)

    suspend fun getMatches(): Resource<List<Match>> {
        Log.d(TAG, "getMatches: calling real API at ${RetrofitClient.BASE_URL}matches")
        return try {
            val response = api.getMatches()
            Log.d(TAG, "getMatches: HTTP ${response.code()} — isSuccessful=${response.isSuccessful}")
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                Log.d(TAG, "getMatches: parsed ${data.size} matches successfully")
                Resource.Success(data)
            } else {
                val errBody = response.errorBody()?.string() ?: "(empty)"
                Log.e(TAG, "getMatches: error ${response.code()} — $errBody")
                Resource.Error("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMatches: exception", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun createMatch(
        homeTeamId: String, awayTeamId: String,
        date: String, time: String, location: String, leagueId: String
    ): Resource<Match> {
        return try {
            val request = CreateMatchRequest(homeTeamId, awayTeamId, date, time, location, leagueId)
            val response = api.createMatch(request)
            if (response.isSuccessful && response.body()?.data != null)
                Resource.Success(response.body()!!.data!!)
            else Resource.Error(response.message() ?: "Failed to create match")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateScore(
        id: String, homeScore: Int, awayScore: Int,
        status: String, scorers: List<GoalEvent> = emptyList()
    ): Resource<Match> {
        return try {
            val response = api.updateScore(id, UpdateScoreRequest(homeScore, awayScore, status, scorers))
            if (response.isSuccessful && response.body()?.data != null)
                Resource.Success(response.body()!!.data!!)
            else Resource.Error(response.message() ?: "Failed to update score")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun deleteMatch(id: String): Resource<Unit> {
        return try {
            val response = api.deleteMatch(id)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(response.message() ?: "Failed to delete match")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
