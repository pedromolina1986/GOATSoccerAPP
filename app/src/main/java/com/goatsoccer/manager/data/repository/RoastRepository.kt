package com.goatsoccer.manager.data.repository

import com.goatsoccer.manager.data.api.RetrofitClient
import com.goatsoccer.manager.data.local.SessionManager
import com.goatsoccer.manager.data.model.CreateRoastRequest
import com.goatsoccer.manager.data.model.Roast
import com.goatsoccer.manager.util.Resource

class RoastRepository(private val sessionManager: SessionManager) {

    private val api = RetrofitClient.create(sessionManager)

    suspend fun getRoasts(): Resource<List<Roast>> {
        return try {
            val response = api.getRoasts()
            if (response.isSuccessful && response.body() != null)
                Resource.Success(response.body()!!.data)
            else Resource.Error(response.message() ?: "Failed to fetch roasts")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun createRoast(targetType: String, targetId: String, content: String): Resource<Roast> {
        return try {
            val response = api.createRoast(CreateRoastRequest(targetType, targetId, content))
            if (response.isSuccessful && response.body()?.data != null)
                Resource.Success(response.body()!!.data!!)
            else Resource.Error(response.message() ?: "Failed to post roast")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun deleteRoast(id: String): Resource<Unit> {
        return try {
            val response = api.deleteRoast(id)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(response.message() ?: "Failed to delete roast")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
