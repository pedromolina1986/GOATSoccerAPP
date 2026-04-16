package com.goatsoccer.manager.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    // Makes LiveData execute synchronously on the calling thread
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock()
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    fun `login with blank email sets error without calling repository`() {
        viewModel.login("", "password123")

        val result = viewModel.loginResult.value
        assertTrue(result is Resource.Error)
        assertEquals("Email and password are required", (result as Resource.Error).message)
    }

    @Test
    fun `login with blank password sets error without calling repository`() {
        viewModel.login("user@example.com", "")

        val result = viewModel.loginResult.value
        assertTrue(result is Resource.Error)
        assertEquals("Email and password are required", (result as Resource.Error).message)
    }

    @Test
    fun `login with both blank sets error without calling repository`() = runTest {
        viewModel.login("", "")

        val result = viewModel.loginResult.value
        assertTrue(result is Resource.Error)
        verify(authRepository, never()).login(any(), any())
    }

    @Test
    fun `login with whitespace-only email sets error`() {
        viewModel.login("   ", "password123")

        assertTrue(viewModel.loginResult.value is Resource.Error)
    }

    // ── Repository delegation ─────────────────────────────────────────────────

    @Test
    fun `login with valid inputs calls repository with same credentials`() = runTest {
        whenever(authRepository.login("user@example.com", "password123"))
            .thenReturn(Resource.Success("fake_token"))

        viewModel.login("user@example.com", "password123")

        verify(authRepository).login("user@example.com", "password123")
    }

    @Test
    fun `login success propagates token to loginResult`() = runTest {
        whenever(authRepository.login(any(), any()))
            .thenReturn(Resource.Success("jwt_token_abc"))

        viewModel.login("user@example.com", "secret123")

        val result = viewModel.loginResult.value
        assertTrue(result is Resource.Success)
        assertEquals("jwt_token_abc", (result as Resource.Success).data)
    }

    @Test
    fun `login repository error propagates message to loginResult`() = runTest {
        whenever(authRepository.login(any(), any()))
            .thenReturn(Resource.Error("Invalid credentials"))

        viewModel.login("user@example.com", "wrongpass")

        val result = viewModel.loginResult.value
        assertTrue(result is Resource.Error)
        assertEquals("Invalid credentials", (result as Resource.Error).message)
    }

    @Test
    fun `login repository network error propagates to loginResult`() = runTest {
        whenever(authRepository.login(any(), any()))
            .thenReturn(Resource.Error("Network error. Make sure the server is running."))

        viewModel.login("user@example.com", "pass123")

        val result = viewModel.loginResult.value
        assertTrue(result is Resource.Error)
    }

    // ── isLoggedIn ────────────────────────────────────────────────────────────

    @Test
    fun `isLoggedIn returns true when repository returns true`() {
        whenever(authRepository.isLoggedIn()).thenReturn(true)

        assertTrue(viewModel.isLoggedIn())
    }

    @Test
    fun `isLoggedIn returns false when repository returns false`() {
        whenever(authRepository.isLoggedIn()).thenReturn(false)

        assertFalse(viewModel.isLoggedIn())
    }
}
