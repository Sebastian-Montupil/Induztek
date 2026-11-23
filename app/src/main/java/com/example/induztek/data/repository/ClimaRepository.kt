package com.example.induztek.data.repository

import com.example.induztek.data.remote.clima.ClimaActual
import com.example.induztek.data.remote.clima.ClimaApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ClimaRepository(
    private val api: ClimaApi = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ClimaApi::class.java)
) {

    suspend fun obtenerClimaActual(): ClimaActual {
        return api.obtenerClima().current
    }
}
