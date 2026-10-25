package com.example.induztek.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "visitas",
    indices = [
        Index(value = ["numeroOt"], unique = true),
        Index(value = ["clienteId"]),
        Index(value = ["instalacionId"]),
        Index(value = ["ingenieroCargoId"])
    ]
)
data class VisitaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clienteId: String,
    val instalacionId: String,
    val fechaMillis: Long,
    val numeroOt: String,
    val tipoVisita: String,
    val ingenieroCargoId: String,
    val observaciones: String
)
