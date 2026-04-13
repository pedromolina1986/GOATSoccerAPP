package com.goatsoccer.manager.data.model

import com.google.gson.annotations.SerializedName

data class GoalEvent(
    @SerializedName("playerId") val playerId: String = "",
    @SerializedName("playerName") val playerName: String = "",
    @SerializedName("teamId") val teamId: String = ""
)

data class Match(
    @SerializedName("_id") val id: String = "",
    @SerializedName("homeTeam") val homeTeam: Team = Team(),
    @SerializedName("awayTeam") val awayTeam: Team = Team(),
    @SerializedName("homeScore") val homeScore: Int = 0,
    @SerializedName("awayScore") val awayScore: Int = 0,
    @SerializedName("date") val date: String = "",
    @SerializedName("time") val time: String = "",
    @SerializedName("location") val location: String = "",
    @SerializedName("status") val status: String = "scheduled", // scheduled, live, finished
    @SerializedName("leagueId") val leagueId: String = "",
    @SerializedName("goalScorers") val goalScorers: List<GoalEvent> = emptyList()
)

data class CreateMatchRequest(
    @SerializedName("homeTeamId") val homeTeamId: String,
    @SerializedName("awayTeamId") val awayTeamId: String,
    @SerializedName("date") val date: String,
    @SerializedName("time") val time: String,
    @SerializedName("location") val location: String,
    @SerializedName("leagueId") val leagueId: String
)

data class UpdateScoreRequest(
    @SerializedName("homeScore") val homeScore: Int,
    @SerializedName("awayScore") val awayScore: Int,
    @SerializedName("status") val status: String,
    @SerializedName("scorers") val scorers: List<GoalEvent> = emptyList()
)
