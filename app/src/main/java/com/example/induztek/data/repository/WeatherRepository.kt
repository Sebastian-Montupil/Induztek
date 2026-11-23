package com.example.induztek.data.repository

import com.example.induztek.network.WeatherApi
import com.example.induztek.network.WeatherResponseDto
import com.example.induztek.network.WeatherRetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val weatherApi: WeatherApi = WeatherRetrofitClient.api,
) {
    suspend fun consultarClima(): WeatherResponseDto {
        return withContext(Dispatchers.IO) {
            weatherApi.obtenerClima()
        }
    }
}
