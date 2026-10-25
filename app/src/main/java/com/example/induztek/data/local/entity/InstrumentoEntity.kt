package com.example.induztek.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "instrumentos_medicion",
    indices = [Index(value = ["numeroSerie"], unique = true)],
)
data class InstrumentoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val tipoInstrumento: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val codigoInterno: String,
    val fechaVencimientoCalibracion: String? = null,
    val observaciones: String,
)
