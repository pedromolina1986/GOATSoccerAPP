package com.goatsoccer.manager.data.model

import com.google.gson.annotations.SerializedName

data class Standing(
    @SerializedName("_id") val id: String = "",
    @SerializedName("team") val team: Team = Team(),
    @SerializedName("leagueId") val leagueId: String = "",
    @SerializedName("played") val played: Int = 0,
    @SerializedName("won") val won: Int = 0,
    @SerializedName("drawn") val drawn: Int = 0,
    @SerializedName("lost") val lost: Int = 0,
    @SerializedName("goalsFor") val goalsFor: Int = 0,
    @SerializedName("goalsAgainst") val goalsAgainst: Int = 0,
    @SerializedName("goalDifference") val goalDifference: Int = 0,
    @SerializedName("points") val points: Int = 0,
    @SerializedName("rank") val rank: Int = 0
)
