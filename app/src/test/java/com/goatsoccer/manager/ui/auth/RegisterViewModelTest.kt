package com.goatsoccer.manager.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.goatsoccer.manager.data.model.InviteLookupResult
import com.goatsoccer.manager.data.repository.AuthRepository
import com.goatsoccer.manager.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock()
        viewModel = RegisterViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Name validation ───────────────────────────────────────────────────────

    @Test
    fun `register without invite and blank name sets name required error`() {
        viewModel.register("", "user@test.com", "pass123", "pass123", "fan")

        val result = viewModel.registerResult.value
        assertTrue(result is Resource.Error)
        assertEquals("Name is required", (result as Resource.Error).message)
    }

    @Test
    fun `register without invite and whitespace name sets name required error`() {
        viewModel.register("   ", "user@test.com", "pass123", "pass123", "fan")

        assertTrue(viewModel.registerResult.value is Resource.Error)
        assertEquals("Name is required", (viewModel.registerResult.value as Resource.Error).message)
    }

    // ── Email validation ──────────────────────────────────────────────────────

    @Test
    fun `register with blank email sets email required error`() {
        viewModel.register("John", "", "pass123", "pass123", "fan")

        val result = viewModel.registerResult.value
        assertTrue(result is Resource.Error)
        assertEquals("Email is required", (result as Resource.Error).message)
    }

    // ── Password validation ───────────────────────────────────────────────────

    @Test
    fun `register with blank password sets password required error`() {
        viewModel.register("John", "john@test.com", "", "", "fan")

        val result = viewModel.registerResult.value
        assertTrue(result is Resource.Error)
        assertEquals("Password is required", (result as Resource.Error).message)
    }

    @Test
    fun `register with mismatched passwords sets passwords do not match error`() {
        viewModel.register("John", "john@test.com", "pass123", "pass456", "fan")

        val result = viewModel.registerResult.value
        assertTrue(result is Resource.Error)
        assertEquals("Passwords do not match", (result as Resource.Error).message)
    }

    @Test
    fun `register with password shorter than 6 chars sets minimum length error`() {
        viewModel.register("John", "john@test.com", "abc", "abc", "fan")

        val result = viewModel.registerResult.value
        assertTrue(result is Resource.Error)
        assertEquals("Password must be at least 6 characters", (result as Resource.Error).message)
    }

    @Test
    fun `register with exactly 6 char password passes length check`() = runTest {
        whenever(authRepository.register(any(), any(), any(), any()))
            .thenReturn(Resource.Success("token"))

        viewModel.register("John", "john@test.com", "abc123", "abc123", "fan")

        // Validation passed — repository was called
        verify(authRepository).register("John", "john@test.com", "abc123", "fan")
    }

    // ── Successful registration ───────────────────────────────────────────────

    @Test
    fun `register with valid inputs calls repository with correct arguments`() = runTest {
        whenever(authRepository.register(any(), any(), any(), any()))
            .thenReturn(Resource.Success("jwt_token"))

        viewModel.register("John", "john@test.com", "password", "password", "coach")

        verify(authRepository).register("John", "john@test.com", "password", "coach")
    }

    @Test
    fun `register success sets Success resource`() = runTest {
        whenever(authRepository.register(any(), any(), any(), any()))
            .thenReturn(Resource.Success("jwt_token"))

        viewModel.register("John", "john@test.com", "password", "password", "coach")

        assertTrue(viewModel.registerResult.value is Resource.Success)
    }

    @Test
    fun `register repository error propagates message`() = runTest {
        whenever(authRepository.register(any(), any(), any(), any()))
            .thenReturn(Resource.Error("Email already in use"))

        viewModel.register("John", "john@test.com", "password", "password", "fan")

        val result = viewModel.registerResult.value
        assertTrue(result is Resource.Error)
        assertEquals("Email already in use", (result as Resource.Error).message)
    }

    // ── Invite flow ───────────────────────────────────────────────────────────

    @Test
    fun `invited player register uses invite name and forces player role`() = runTest {
        whenever(authRepository.lookupInvite("invited@test.com"))
            .thenReturn(InviteLookupResult(found = true, name = "Jane Doe", teamName = "FC Goal"))
        whenever(authRepository.register(any(), any(), any(), any()))
            .thenReturn(Resource.Success("token"))

        viewModel.lookupInvite("invited@test.com")
        // name "" and role "fan" are overridden by the invite
        viewModel.register("", "invited@test.com", "securepass", "securepass", "fan")

        verify(authRepository).register("Jane Doe", "invited@test.com", "securepass", "player")
    }

    @Test
    fun `invited player register skips name validation`() = runTest {
        whenever(authRepository.lookupInvite("invited@test.com"))
            .thenReturn(InviteLookupResult(found = true, name = "Jane Doe", teamName = "FC Goal"))
        whenever(authRepository.register(any(), any(), any(), any()))
            .thenReturn(Resource.Success("token"))

        viewModel.lookupInvite("invited@test.com")
        // blank name is allowed because invite provides it
        viewModel.register("", "invited@test.com", "securepass", "securepass", "fan")

        // Should not emit a "Name is required" error
        assertFalse(viewModel.registerResult.value is Resource.Error)
    }

    // ── lookupInvite ──────────────────────────────────────────────────────────

    @Test
    fun `lookupInvite with blank email does nothing`() = runTest {
        viewModel.lookupInvite("")

        verifyNoInteractions(authRepository)
    }

    @Test
    fun `lookupInvite result is exposed via inviteLookup LiveData`() = runTest {
        val expected = InviteLookupResult(found = true, name = "Bob", teamName = "FC Test")
        whenever(authRepository.lookupInvite("bob@test.com")).thenReturn(expected)

        viewModel.lookupInvite("bob@test.com")

        assertEquals(expected, viewModel.inviteLookup.value)
    }

    @Test
    fun `lookupInvite not found sets found=false in LiveData`() = runTest {
        whenever(authRepository.lookupInvite("unknown@test.com"))
            .thenReturn(InviteLookupResult(found = false))

        viewModel.lookupInvite("unknown@test.com")

        assertFalse(viewModel.inviteLookup.value?.found ?: true)
    }
}
