package com.goatsoccer.manager.data.model

import com.google.gson.annotations.SerializedName

data class Roast(
    @SerializedName("_id") val id: String = "",
    @SerializedName("author") val author: User = User(),
    @SerializedName("targetType") val targetType: String = "match", // match, player, team
    @SerializedName("targetId") val targetId: String = "",
    @SerializedName("content") val content: String = "",
    @SerializedName("likes") val likes: Int = 0,
    @SerializedName("createdAt") val createdAt: String = ""
)

data class CreateRoastRequest(
    @SerializedName("targetType") val targetType: String,
    @SerializedName("targetId") val targetId: String,
    @SerializedName("content") val content: String
)
