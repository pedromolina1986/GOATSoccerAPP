package com.goatsoccer.manager.data.model

import com.google.gson.annotations.SerializedName

data class PlayerLookupResult(
    @SerializedName("found")  val found: Boolean,
    @SerializedName("user")   val user: User?   = null,
    @SerializedName("player") val player: Player? = null
)

data class InvitePlayerRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("name")     val name: String     = "",
    @SerializedName("position") val position: String  = "",
    @SerializedName("number")   val number: Int       = 0
)

data class InviteLookupResult(
    @SerializedName("found")    val found: Boolean,
    @SerializedName("name")     val name: String     = "",
    @SerializedName("teamName") val teamName: String  = ""
)
