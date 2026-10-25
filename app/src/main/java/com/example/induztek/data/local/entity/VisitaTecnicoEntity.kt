package com.example.induztek.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "visita_tecnicos",
    primaryKeys = ["visitaId", "tecnicoId"],
    foreignKeys = [
        ForeignKey(
            entity = VisitaEntity::class,
            parentColumns = ["id"],
            childColumns = ["visitaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tecnicoId"])
    ]
)
data class VisitaTecnicoEntity(
    val visitaId: Long,
    val tecnicoId: String
)
