package com.goatsoccer.manager.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for SessionManager.
 * Runs against real SharedPreferences on a device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class SessionManagerTest {

    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        sessionManager = SessionManager(context)
        sessionManager.clearSession()          // always start from a clean state
    }

    @After
    fun tearDown() {
        sessionManager.clearSession()
    }

    // ── Default / empty state ─────────────────────────────────────────────────

    @Test
    fun isLoggedIn_returnsFalse_byDefault() {
        assertFalse(sessionManager.isLoggedIn())
    }

    @Test
    fun getAuthToken_returnsNull_byDefault() {
        assertNull(sessionManager.getAuthToken())
    }

    @Test
    fun getUserId_returnsNull_byDefault() {
        assertNull(sessionManager.getUserId())
    }

    @Test
    fun getMyTeamId_returnsEmptyString_byDefault() {
        assertEquals("", sessionManager.getMyTeamId())
    }

    // ── Token storage ─────────────────────────────────────────────────────────

    @Test
    fun saveAuthToken_andGetAuthToken_roundTrip() {
        sessionManager.saveAuthToken("my_jwt_token_12345")

        assertEquals("my_jwt_token_12345", sessionManager.getAuthToken())
    }

    @Test
    fun saveAuthToken_overwritesPreviousToken() {
        sessionManager.saveAuthToken("old_token")
        sessionManager.saveAuthToken("new_token")

        assertEquals("new_token", sessionManager.getAuthToken())
    }

    // ── User storage ──────────────────────────────────────────────────────────

    @Test
    fun saveUser_setsIsLoggedInToTrue() {
        sessionManager.saveUser("id1", "John", "john@test.com", "coach", "team1")

        assertTrue(sessionManager.isLoggedIn())
    }

    @Test
    fun saveUser_storesAllFields() {
        sessionManager.saveUser("id1", "John Doe", "john@test.com", "coach", "team123")

        assertEquals("id1",            sessionManager.getUserId())
        assertEquals("John Doe",       sessionManager.getUserName())
        assertEquals("john@test.com",  sessionManager.getUserEmail())
        assertEquals("coach",          sessionManager.getUserRole())
        assertEquals("team123",        sessionManager.getMyTeamId())
    }

    @Test
    fun saveUser_withoutTeamId_storesEmptyTeamId() {
        sessionManager.saveUser("id2", "Jane", "jane@test.com", "fan")

        assertEquals("", sessionManager.getMyTeamId())
    }

    // ── clearSession ──────────────────────────────────────────────────────────

    @Test
    fun clearSession_setsIsLoggedInToFalse() {
        sessionManager.saveUser("id1", "John", "john@test.com", "fan")
        assertTrue(sessionManager.isLoggedIn())

        sessionManager.clearSession()

        assertFalse(sessionManager.isLoggedIn())
    }

    @Test
    fun clearSession_removesAuthToken() {
        sessionManager.saveAuthToken("token_to_be_removed")
        sessionManager.clearSession()

        assertNull(sessionManager.getAuthToken())
    }

    @Test
    fun clearSession_removesUserData() {
        sessionManager.saveUser("id1", "John", "john@test.com", "coach", "team1")
        sessionManager.clearSession()

        assertNull(sessionManager.getUserId())
        assertNull(sessionManager.getUserName())
        assertNull(sessionManager.getUserEmail())
    }

    // ── Role helpers ──────────────────────────────────────────────────────────

    @Test
    fun isCoach_returnsTrue_whenRoleIsCoach() {
        sessionManager.saveUser("1", "Bob", "bob@test.com", "coach")

        assertTrue(sessionManager.isCoach())
        assertFalse(sessionManager.isPlayer())
        assertFalse(sessionManager.isFan())
    }

    @Test
    fun isPlayer_returnsTrue_whenRoleIsPlayer() {
        sessionManager.saveUser("1", "Alice", "alice@test.com", "player")

        assertTrue(sessionManager.isPlayer())
        assertFalse(sessionManager.isCoach())
        assertFalse(sessionManager.isFan())
    }

    @Test
    fun isFan_returnsTrue_whenRoleIsFan() {
        sessionManager.saveUser("1", "Charlie", "charlie@test.com", "fan")

        assertTrue(sessionManager.isFan())
        assertFalse(sessionManager.isCoach())
        assertFalse(sessionManager.isPlayer())
    }

    // ── Team ID convenience ───────────────────────────────────────────────────

    @Test
    fun saveMyTeamId_andGetMyTeamId_roundTrip() {
        sessionManager.saveMyTeamId("team_xyz_456")

        assertEquals("team_xyz_456", sessionManager.getMyTeamId())
    }

    @Test
    fun saveMyTeamId_overwritesPreviousTeamId() {
        sessionManager.saveMyTeamId("old_team")
        sessionManager.saveMyTeamId("new_team")

        assertEquals("new_team", sessionManager.getMyTeamId())
    }
}
