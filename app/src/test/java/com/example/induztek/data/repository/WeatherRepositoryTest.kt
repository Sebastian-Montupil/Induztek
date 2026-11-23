package com.example.induztek.data.repository

import com.example.induztek.network.CurrentWeatherDto
import com.example.induztek.network.WeatherApi
import com.example.induztek.network.WeatherResponseDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WeatherRepositoryTest {

    private class FakeWeatherApi(
        private val responseToReturn: WeatherResponseDto,
        private val shouldThrow: Boolean = false,
    ) : WeatherApi {
        override suspend fun obtenerClima(
            latitude: Double,
            longitude: Double,
            current: String,
            timezone: String,
        ): WeatherResponseDto {
            if (shouldThrow) {
                throw RuntimeException("Error de red simulado")
            }
            return responseToReturn
        }
    }

    @Test
    fun consultarClima_exitoso_retornaDatos() {
        runBlocking {
            val fakeDto = WeatherResponseDto(
                latitude = -33.45,
                longitude = -70.66,
                current = CurrentWeatherDto(
                    time = "2025-01-01T12:00",
                    temperature2m = 25.5,
                    relativeHumidity2m = 50,
                    weatherCode = 0,
                    windSpeed10m = 10.0,
                ),
            )
            val repo = WeatherRepository(FakeWeatherApi(fakeDto))
            val resultado = repo.consultarClima()

            assertNotNull(resultado.current)
            assertEquals(25.5, resultado.current?.temperature2m)
            assertEquals(0, resultado.current?.weatherCode)
        }
    }

    @Test(expected = RuntimeException::class)
    fun consultarClima_error_lanzaExcepcion() {
        runBlocking {
            val repo = WeatherRepository(FakeWeatherApi(WeatherResponseDto(), shouldThrow = true))
            repo.consultarClima()
        }
    }
}
