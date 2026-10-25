package com.example.induztek.model

enum class RolPersonal {
    INGENIERO,
    TECNICO
}

data class Personal(
    val id: String,
    val nombreCompleto: String,
    val rol: RolPersonal
)

object CatalogoPersonal {
    val ingenieros = listOf(
        Personal("ING-01", "Andrea Contreras Silva", RolPersonal.INGENIERO),
        Personal("ING-02", "Sebastián Morales Gutiérrez", RolPersonal.INGENIERO)
    )

    val tecnicos = listOf(
        Personal("TEC-01", "Matías Godoy Herrera", RolPersonal.TECNICO),
        Personal("TEC-02", "Javiera Tapia Muñoz", RolPersonal.TECNICO),
        Personal("TEC-03", "Carlos Rondón Pérez", RolPersonal.TECNICO),
        Personal("TEC-04", "Diego Valenzuela Ríos", RolPersonal.TECNICO)
    )
}
