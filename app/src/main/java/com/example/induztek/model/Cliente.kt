package com.example.induztek.model

data class Cliente(
    val id: String,
    val nombre: String
)

data class Instalacion(
    val id: String,
    val clienteId: String,
    val nombre: String
)

object CatalogoClientes {
    val clientes = listOf(
        Cliente("CLI-01", "Envasadora del Pacífico S.A."),
        Cliente("CLI-02", "Sanitaria Valle Central S.A."),
        Cliente("CLI-03", "Minera Cerro Alto SpA"),
        Cliente("CLI-04", "Clínica Los Aromos Ltda."),
        Cliente("CLI-05", "Cementos Lomas Blancas S.A."),
        Cliente("CLI-06", "Data Center Andes Norte SpA")
    )

    val instalaciones = listOf(
        Instalacion("INS-01", "CLI-01", "Planta Lampa"),
        Instalacion("INS-02", "CLI-02", "Planta Elevadora Sur"),
        Instalacion("INS-03", "CLI-03", "Faena Til Til"),
        Instalacion("INS-04", "CLI-04", "Edificio Clínico"),
        Instalacion("INS-05", "CLI-05", "Planta Molienda"),
        Instalacion("INS-06", "CLI-06", "Site Huechuraba")
    )

    fun getInstalacionesPorCliente(clienteId: String?): List<Instalacion> {
        if (clienteId == null) return emptyList()
        return instalaciones.filter { it.clienteId == clienteId }
    }
}
