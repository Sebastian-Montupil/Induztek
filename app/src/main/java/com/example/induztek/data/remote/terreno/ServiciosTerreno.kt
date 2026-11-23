package com.example.induztek.data.remote.terreno

import com.example.induztek.network.RetrofitClient

object ServiciosTerreno {

    val visitasApi: VisitasApi by lazy {
        RetrofitClient.crearServicio(
            VisitasApi::class.java
        )
    }

    val equiposApi: EquiposApi by lazy {
        RetrofitClient.crearServicio(
            EquiposApi::class.java
        )
    }

    val instrumentosApi: InstrumentosApi by lazy {
        RetrofitClient.crearServicio(
            InstrumentosApi::class.java
        )
    }

    val calibracionesApi: CalibracionesApi by lazy {
        RetrofitClient.crearServicio(
            CalibracionesApi::class.java
        )
    }

    val visitas: VisitasRemoteDataSource by lazy {
        VisitasRemoteDataSource(visitasApi)
    }

    val equipos: EquiposRemoteDataSource by lazy {
        EquiposRemoteDataSource(equiposApi)
    }

    val instrumentos: InstrumentosRemoteDataSource by lazy {
        InstrumentosRemoteDataSource(instrumentosApi)
    }

    val calibraciones: CalibracionesRemoteDataSource by lazy {
        CalibracionesRemoteDataSource(calibracionesApi)
    }
}
