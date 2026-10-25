package com.example.induztek.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "equipos",
    indices = [
        Index(value = ["numeroSerie"], unique = true),
        Index(value = ["clienteId"]),
        Index(value = ["instalacionId"]),
    ],
)
data class EquipoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clienteId: String,
    val instalacionId: String,
    val nombre: String,
    val tipo: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val codigoInterno: String,
    val observaciones: String,
)
