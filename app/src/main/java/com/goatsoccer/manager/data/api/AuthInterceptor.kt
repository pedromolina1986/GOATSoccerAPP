package com.goatsoccer.manager.data.api

import com.goatsoccer.manager.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.getAuthToken()
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            }
            addHeader("Content-Type", "application/json")
        }.build()
        return chain.proceed(request)
    }
}
