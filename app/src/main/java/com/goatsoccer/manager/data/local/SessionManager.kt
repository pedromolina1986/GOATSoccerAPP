package com.goatsoccer.manager.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "goat_soccer_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_MY_TEAM_ID = "my_team_id"
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getAuthToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUser(id: String, name: String, email: String, role: String, teamId: String = "") {
        prefs.edit()
            .putString(KEY_USER_ID, id)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_MY_TEAM_ID, teamId)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, "fan")
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getMyTeamId(): String = prefs.getString(KEY_MY_TEAM_ID, "") ?: ""
    fun saveMyTeamId(teamId: String) { prefs.edit().putString(KEY_MY_TEAM_ID, teamId).apply() }

    fun isCoach(): Boolean = getUserRole() == "coach"
    fun isPlayer(): Boolean = getUserRole() == "player"
    fun isFan(): Boolean = getUserRole() == "fan"

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
