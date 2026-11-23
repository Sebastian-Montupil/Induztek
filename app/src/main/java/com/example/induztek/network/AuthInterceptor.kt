package com.example.induztek.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authHeader = SesionManager.obtenerAutorizacion()
        val request = if (authHeader != null) {
            originalRequest.newBuilder()
                .header("Authorization", authHeader)
                .build()
        } else {
            originalRequest
        }
        return chain.proceed(request)
    }
}
