package com.goatsoccer.manager.ui.teams

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.goatsoccer.manager.data.model.Team
import com.goatsoccer.manager.data.repository.LeagueRepository
import com.goatsoccer.manager.data.repository.TeamRepository
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
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class TeamsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var teamRepository: TeamRepository
    private lateinit var leagueRepository: LeagueRepository
    private lateinit var viewModel: TeamsViewModel

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(testDispatcher)
        teamRepository = mock()
        leagueRepository = mock()
        // Stub init-time calls so the ViewModel can be constructed cleanly
        whenever(teamRepository.getTeams()).thenReturn(Resource.Success(emptyList()))
        whenever(leagueRepository.getLeagues()).thenReturn(Resource.Success(emptyList()))
        viewModel = TeamsViewModel(teamRepository, leagueRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── init ──────────────────────────────────────────────────────────────────

    @Test
    fun `ViewModel fetches teams and leagues on init`() = runTest {
        verify(teamRepository, atLeastOnce()).getTeams()
        verify(leagueRepository, atLeastOnce()).getLeagues()
    }

    // ── createTeam validation ─────────────────────────────────────────────────

    @Test
    fun `createTeam with blank name sets error and never calls repository`() = runTest {
        viewModel.createTeam("", "Calgary", "Coach Bob")

        val result = viewModel.createResult.value
        assertTrue(result is Resource.Error)
        assertEquals("Team name is required", (result as Resource.Error).message)
        verify(teamRepository, never()).createTeam(any(), any(), any())
    }

    @Test
    fun `createTeam with whitespace-only name sets error`() = runTest {
        viewModel.createTeam("   ", "Calgary", "Coach Bob")

        assertTrue(viewModel.createResult.value is Resource.Error)
        verify(teamRepository, never()).createTeam(any(), any(), any())
    }

    // ── createTeam success ────────────────────────────────────────────────────

    @Test
    fun `createTeam with valid name calls repository with correct arguments`() = runTest {
        val fakeTeam = Team(id = "1", name = "FC Goat", city = "Calgary", coach = "Bob")
        whenever(teamRepository.createTeam("FC Goat", "Calgary", "Bob"))
            .thenReturn(Resource.Success(fakeTeam))

        viewModel.createTeam("FC Goat", "Calgary", "Bob")

        verify(teamRepository).createTeam("FC Goat", "Calgary", "Bob")
    }

    @Test
    fun `createTeam success sets Success resource with returned team`() = runTest {
        val fakeTeam = Team(id = "42", name = "FC Goat", city = "Calgary", coach = "Bob")
        whenever(teamRepository.createTeam(any(), any(), any()))
            .thenReturn(Resource.Success(fakeTeam))

        viewModel.createTeam("FC Goat", "Calgary", "Bob")

        val result = viewModel.createResult.value
        assertTrue(result is Resource.Success)
        assertEquals("FC Goat", (result as Resource.Success).data?.name)
    }

    @Test
    fun `createTeam repository error propagates to createResult`() = runTest {
        whenever(teamRepository.createTeam(any(), any(), any()))
            .thenReturn(Resource.Error("Server error"))

        viewModel.createTeam("FC Goat", "Calgary", "Bob")

        val result = viewModel.createResult.value
        assertTrue(result is Resource.Error)
        assertEquals("Server error", (result as Resource.Error).message)
    }

    // ── fetchTeams ────────────────────────────────────────────────────────────

    @Test
    fun `fetchTeams exposes team list on success`() = runTest {
        val teams = listOf(
            Team(id = "1", name = "Team Alpha"),
            Team(id = "2", name = "Team Beta")
        )
        whenever(teamRepository.getTeams()).thenReturn(Resource.Success(teams))

        viewModel.fetchTeams()

        val result = viewModel.teams.value
        assertTrue(result is Resource.Success)
        assertEquals(2, (result as Resource.Success).data?.size)
        assertEquals("Team Alpha", result.data?.first()?.name)
    }

    @Test
    fun `fetchTeams exposes Error on repository failure`() = runTest {
        whenever(teamRepository.getTeams()).thenReturn(Resource.Error("No network"))

        viewModel.fetchTeams()

        assertTrue(viewModel.teams.value is Resource.Error)
    }

    // ── deleteTeam ────────────────────────────────────────────────────────────

    @Test
    fun `deleteTeam calls repository with the correct team id`() = runTest {
        whenever(teamRepository.deleteTeam("team123")).thenReturn(Resource.Success(Unit))

        viewModel.deleteTeam("team123")

        verify(teamRepository).deleteTeam("team123")
    }

    @Test
    fun `deleteTeam success sets deleteResult to Success`() = runTest {
        whenever(teamRepository.deleteTeam(any())).thenReturn(Resource.Success(Unit))

        viewModel.deleteTeam("team123")

        assertTrue(viewModel.deleteResult.value is Resource.Success)
    }
}
