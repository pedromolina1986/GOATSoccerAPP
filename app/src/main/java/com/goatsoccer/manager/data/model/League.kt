package com.goatsoccer.manager.data.model

import com.google.gson.annotations.SerializedName

data class League(
    @SerializedName("_id")      val id: String       = "",
    @SerializedName("name")     val name: String     = "",
    @SerializedName("season")   val season: String   = "",
    @SerializedName("coachId")  val coachId: String  = "",
    @SerializedName("teamIds")  val teamIds: List<String> = emptyList()
)

data class CreateLeagueRequest(
    @SerializedName("name")   val name: String,
    @SerializedName("season") val season: String
)

data class AddTeamToLeagueRequest(
    @SerializedName("teamId") val teamId: String
)
