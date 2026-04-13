package com.goatsoccer.manager.data.api

import com.goatsoccer.manager.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────
    @GET("auth/lookup-invite")
    suspend fun lookupInvite(@Query("email") email: String): Response<ApiResponse<InviteLookupResult>>

    @GET("auth/me")
    suspend fun getMe(): Response<ApiResponse<User>>

    @DELETE("auth/me")
    suspend fun deleteAccount(): Response<ApiResponse<Unit>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // ── Teams ─────────────────────────────────────────────────────────────
    @GET("teams")
    suspend fun getTeams(): Response<ListResponse<Team>>

    @GET("teams/{id}")
    suspend fun getTeamById(@Path("id") id: String): Response<ApiResponse<Team>>

    @POST("teams")
    suspend fun createTeam(@Body request: CreateTeamRequest): Response<ApiResponse<Team>>

    @PUT("teams/{id}")
    suspend fun updateTeam(
        @Path("id") id: String,
        @Body request: CreateTeamRequest
    ): Response<ApiResponse<Team>>

    @DELETE("teams/{id}")
    suspend fun deleteTeam(@Path("id") id: String): Response<ApiResponse<Unit>>

    // ── Players ───────────────────────────────────────────────────────────
    @GET("teams/{teamId}/players")
    suspend fun getPlayersByTeam(@Path("teamId") teamId: String): Response<ListResponse<Player>>

    @POST("teams/{teamId}/players")
    suspend fun addPlayer(
        @Path("teamId") teamId: String,
        @Body request: CreatePlayerRequest
    ): Response<ApiResponse<Player>>

    @PUT("teams/{teamId}/players/{playerId}")
    suspend fun updatePlayer(
        @Path("teamId") teamId: String,
        @Path("playerId") playerId: String,
        @Body request: CreatePlayerRequest
    ): Response<ApiResponse<Player>>

    @DELETE("teams/{teamId}/players/{playerId}")
    suspend fun deletePlayer(
        @Path("teamId") teamId: String,
        @Path("playerId") playerId: String
    ): Response<ApiResponse<Unit>>

    @PUT("teams/{teamId}/players/{playerId}/captain")
    suspend fun setCaptain(
        @Path("teamId") teamId: String,
        @Path("playerId") playerId: String
    ): Response<ApiResponse<Unit>>

    @GET("teams/{teamId}/invite/lookup")
    suspend fun lookupPlayerByEmail(
        @Path("teamId") teamId: String,
        @Query("email") email: String
    ): Response<ApiResponse<PlayerLookupResult>>

    @POST("teams/{teamId}/invite")
    suspend fun invitePlayer(
        @Path("teamId") teamId: String,
        @Body request: InvitePlayerRequest
    ): Response<ApiResponse<Player>>

    // ── Matches ───────────────────────────────────────────────────────────
    @GET("matches")
    suspend fun getMatches(): Response<ListResponse<Match>>

    @GET("matches/{id}")
    suspend fun getMatchById(@Path("id") id: String): Response<ApiResponse<Match>>

    @POST("matches")
    suspend fun createMatch(@Body request: CreateMatchRequest): Response<ApiResponse<Match>>

    @PUT("matches/{id}/score")
    suspend fun updateScore(
        @Path("id") id: String,
        @Body request: UpdateScoreRequest
    ): Response<ApiResponse<Match>>

    @DELETE("matches/{id}")
    suspend fun deleteMatch(@Path("id") id: String): Response<ApiResponse<Unit>>

    // ── Leagues ───────────────────────────────────────────────────────────
    @GET("leagues")
    suspend fun getLeagues(): Response<ListResponse<League>>

    @POST("leagues")
    suspend fun createLeague(@Body request: CreateLeagueRequest): Response<ApiResponse<League>>

    @DELETE("leagues/{id}")
    suspend fun deleteLeague(@Path("id") id: String): Response<ApiResponse<Unit>>

    @POST("leagues/{id}/teams")
    suspend fun addTeamToLeague(
        @Path("id") leagueId: String,
        @Body request: AddTeamToLeagueRequest
    ): Response<ApiResponse<League>>

    @DELETE("leagues/{id}/teams/{teamId}")
    suspend fun removeTeamFromLeague(
        @Path("id") leagueId: String,
        @Path("teamId") teamId: String
    ): Response<ApiResponse<League>>

    // ── Standings ─────────────────────────────────────────────────────────
    @GET("standings")
    suspend fun getAllStandings(): Response<ListResponse<Standing>>

    @GET("standings/top-scorers")
    suspend fun getTopScorers(@Query("leagueId") leagueId: String? = null): Response<ListResponse<Player>>

    @GET("standings/{leagueId}")
    suspend fun getStandingsByLeague(@Path("leagueId") leagueId: String): Response<ListResponse<Standing>>

    // ── Roasts ────────────────────────────────────────────────────────────
    @GET("roasts")
    suspend fun getRoasts(): Response<ListResponse<Roast>>

    @POST("roasts")
    suspend fun createRoast(@Body request: CreateRoastRequest): Response<ApiResponse<Roast>>

    @DELETE("roasts/{id}")
    suspend fun deleteRoast(@Path("id") id: String): Response<ApiResponse<Unit>>
}
