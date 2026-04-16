package com.goatsoccer.manager.data.model

import org.junit.Assert.*
import org.junit.Test

class UserModelTest {

    // ── User ─────────────────────────────────────────────────────────────────

    @Test
    fun `User default role is fan`() {
        val user = User()
        assertEquals("fan", user.role)
    }

    @Test
    fun `User default values produce empty strings`() {
        val user = User()
        assertEquals("", user.id)
        assertEquals("", user.name)
        assertEquals("", user.email)
        assertEquals("", user.token)
        assertEquals("", user.teamId)
        assertEquals("", user.createdAt)
    }

    @Test
    fun `User data class equality compares all fields`() {
        val user1 = User(id = "abc", name = "John", email = "john@test.com", role = "coach")
        val user2 = User(id = "abc", name = "John", email = "john@test.com", role = "coach")
        val user3 = User(id = "xyz", name = "John", email = "john@test.com", role = "coach")

        assertEquals(user1, user2)
        assertNotEquals(user1, user3)
    }

    @Test
    fun `User copy preserves unchanged fields`() {
        val original = User(id = "1", name = "Alice", email = "alice@test.com", role = "player")
        val updated = original.copy(name = "Alice Updated")

        assertEquals("Alice Updated", updated.name)
        assertEquals("alice@test.com", updated.email)
        assertEquals("player", updated.role)
        assertEquals("1", updated.id)
    }

    // ── LoginRequest ──────────────────────────────────────────────────────────

    @Test
    fun `LoginRequest stores email and password`() {
        val request = LoginRequest("user@test.com", "password123")

        assertEquals("user@test.com", request.email)
        assertEquals("password123", request.password)
    }

    @Test
    fun `LoginRequest equality works correctly`() {
        val r1 = LoginRequest("a@b.com", "pass")
        val r2 = LoginRequest("a@b.com", "pass")
        val r3 = LoginRequest("a@b.com", "different")

        assertEquals(r1, r2)
        assertNotEquals(r1, r3)
    }

    // ── RegisterRequest ───────────────────────────────────────────────────────

    @Test
    fun `RegisterRequest stores all four fields`() {
        val request = RegisterRequest("Jane", "jane@test.com", "secure123", "player")

        assertEquals("Jane", request.name)
        assertEquals("jane@test.com", request.email)
        assertEquals("secure123", request.password)
        assertEquals("player", request.role)
    }

    // ── AuthResponse ──────────────────────────────────────────────────────────

    @Test
    fun `AuthResponse stores token and user`() {
        val user = User(id = "1", name = "Test", email = "test@test.com", role = "coach")
        val response = AuthResponse(token = "jwt_token_xyz", user = user)

        assertEquals("jwt_token_xyz", response.token)
        assertEquals(user, response.user)
    }

    // ── ErrorBody ─────────────────────────────────────────────────────────────

    @Test
    fun `ErrorBody default message is empty string`() {
        val error = ErrorBody()
        assertEquals("", error.message)
    }

    @Test
    fun `ErrorBody stores provided message`() {
        val error = ErrorBody("Invalid credentials")
        assertEquals("Invalid credentials", error.message)
    }
}
