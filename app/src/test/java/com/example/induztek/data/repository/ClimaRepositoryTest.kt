package com.example.induztek.data.repository

import com.example.induztek.data.remote.clima.ClimaActual
import com.example.induztek.data.remote.clima.ClimaApi
import com.example.induztek.data.remote.clima.ClimaResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ClimaRepositoryTest {

    private class FakeClimaApi(
        private val responseToReturn: ClimaResponse,
        private val shouldThrow: Boolean = false,
    ) : ClimaApi {
        override suspend fun obtenerClima(
            latitud: Double,
            longitud: Double,
            variables: String,
            zonaHoraria: String,
        ): ClimaResponse {
            if (shouldThrow) {
                throw RuntimeException("Error de red simulado")
            }
            return responseToReturn
        }
    }

    @Test
    fun obtenerClimaActual_exitoso_retornaClima() {
        runBlocking {
            val fakeResponse = ClimaResponse(
                current = ClimaActual(
                    temperatura = 22.0,
                    humedad = 60,
                    precipitacion = 0.0,
                    fechaHora = "2025-01-01T12:00",
                ),
            )
            val repo = ClimaRepository(FakeClimaApi(fakeResponse))
            val resultado = repo.obtenerClimaActual()

            assertNotNull(resultado)
            assertEquals(22.0, resultado.temperatura, 0.001)
            assertEquals(60, resultado.humedad)
        }
    }

    @Test(expected = RuntimeException::class)
    fun obtenerClimaActual_error_lanzaExcepcion() {
        runBlocking {
            val emptyResponse = ClimaResponse(current = ClimaActual(0.0, 0, 0.0, ""))
            val repo = ClimaRepository(FakeClimaApi(emptyResponse, shouldThrow = true))
            repo.obtenerClimaActual()
        }
    }
}
