package com.goatsoccer.manager.data.model

import com.google.gson.annotations.SerializedName

data class TeamLeagueStat(
    @SerializedName("leagueId")      val leagueId: String   = "",
    @SerializedName("leagueName")    val leagueName: String  = "",
    @SerializedName("season")        val season: String      = "",
    @SerializedName("rank")          val rank: Int           = 0,
    @SerializedName("played")        val played: Int         = 0,
    @SerializedName("won")           val won: Int            = 0,
    @SerializedName("drawn")         val drawn: Int          = 0,
    @SerializedName("lost")          val lost: Int           = 0,
    @SerializedName("goalsFor")      val goalsFor: Int       = 0,
    @SerializedName("goalsAgainst")  val goalsAgainst: Int   = 0,
    @SerializedName("points")        val points: Int         = 0
)

data class Team(
    @SerializedName("_id")          val id: String                  = "",
    @SerializedName("name")         val name: String                = "",
    @SerializedName("city")         val city: String                = "",
    @SerializedName("logo")         val logo: String                = "",
    @SerializedName("coach")        val coach: String               = "",
    @SerializedName("playerCount")  val playerCount: Int            = 0,
    @SerializedName("wins")         val wins: Int                   = 0,
    @SerializedName("losses")       val losses: Int                 = 0,
    @SerializedName("draws")        val draws: Int                  = 0,
    @SerializedName("points")       val points: Int                 = 0,
    @SerializedName("leagueStats")  val leagueStats: List<TeamLeagueStat> = emptyList()
)

data class CreateTeamRequest(
    @SerializedName("name") val name: String,
    @SerializedName("city") val city: String,
    @SerializedName("coach") val coach: String
)
