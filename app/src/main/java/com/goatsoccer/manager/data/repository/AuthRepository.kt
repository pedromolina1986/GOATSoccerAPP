package com.goatsoccer.manager.data.repository

import com.goatsoccer.manager.data.api.RetrofitClient
import com.goatsoccer.manager.data.local.SessionManager
import com.goatsoccer.manager.data.model.ErrorBody
import com.goatsoccer.manager.data.model.InviteLookupResult
import com.goatsoccer.manager.data.model.LoginRequest
import com.goatsoccer.manager.data.model.RegisterRequest
import com.google.gson.Gson
import com.goatsoccer.manager.util.Resource

class AuthRepository(private val sessionManager: SessionManager) {

    private val api = RetrofitClient.create(sessionManager)

    suspend fun login(email: String, password: String): Resource<String> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.saveAuthToken(body.token)
                sessionManager.saveUser(
                    id = body.user.id,
                    name = body.user.name ?: "",
                    email = body.user.email ?: "",
                    role = body.user.role ?: "fan",
                    teamId = body.user.teamId ?: ""
                )
                Resource.Success(body.token)
            } else {
                val errMsg = try {
                    Gson().fromJson(response.errorBody()?.string(), ErrorBody::class.java).message
                } catch (_: Exception) { null }
                Resource.Error(errMsg?.takeIf { it.isNotBlank() } ?: "Login failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error. Make sure the server is running.")
        }
    }

    suspend fun register(name: String, email: String, password: String, role: String): Resource<String> {
        return try {
            val response = api.register(RegisterRequest(name, email, password, role))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.saveAuthToken(body.token)
                sessionManager.saveUser(
                    id = body.user.id,
                    name = body.user.name ?: "",
                    email = body.user.email ?: "",
                    role = body.user.role ?: "fan",
                    teamId = body.user.teamId ?: ""
                )
                Resource.Success(body.token)
            } else {
                Resource.Error(response.message() ?: "Registration failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error. Make sure the server is running.")
        }
    }

    suspend fun lookupInvite(email: String): InviteLookupResult {
        return try {
            val response = api.lookupInvite(email)
            if (response.isSuccessful) response.body()?.data ?: InviteLookupResult(found = false)
            else InviteLookupResult(found = false)
        } catch (e: Exception) {
            InviteLookupResult(found = false)
        }
    }

    suspend fun getMe(): Resource<com.goatsoccer.manager.data.model.User> {
        return try {
            val response = api.getMe()
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to load profile")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun deactivateAccount(): Resource<Unit> {
        return try {
            val response = api.deleteAccount()
            if (response.isSuccessful) {
                sessionManager.clearSession()
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to deactivate account")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun deleteAccount(): Resource<Unit> = deactivateAccount()

    fun logout() = sessionManager.clearSession()
    fun isLoggedIn() = sessionManager.isLoggedIn()
}
