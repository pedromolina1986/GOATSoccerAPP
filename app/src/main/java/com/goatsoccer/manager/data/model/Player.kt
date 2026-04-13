package com.goatsoccer.manager.data.model

import com.google.gson.annotations.SerializedName

data class Player(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("position") val position: String = "",
    @SerializedName("number") val number: Int = 0,
    @SerializedName("goals") val goals: Int = 0,
    @SerializedName("assists") val assists: Int = 0,
    @SerializedName("yellowCards") val yellowCards: Int = 0,
    @SerializedName("redCards") val redCards: Int = 0,
    @SerializedName("teamId") val teamId: String = "",
    @SerializedName("teamName") val teamName: String = "",
    @SerializedName("isCaptain") val isCaptain: Boolean = false
)

data class CreatePlayerRequest(
    @SerializedName("name") val name: String,
    @SerializedName("position") val position: String,
    @SerializedName("number") val number: Int
)
