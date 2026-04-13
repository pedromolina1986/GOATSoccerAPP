package com.goatsoccer.manager.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String = ""
)

data class ListResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<T> = emptyList(),
    @SerializedName("message") val message: String = "",
    @SerializedName("count") val count: Int = 0
)
