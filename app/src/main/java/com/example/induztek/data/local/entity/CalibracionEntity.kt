package com.example.induztek.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calibraciones",
    foreignKeys = [
        ForeignKey(
            entity = InstrumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["instrumentoId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index(value = ["instrumentoId"])],
)
data class CalibracionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val instrumentoId: Long,
    val fechaCalibracion: String,
    val fechaVencimiento: String,
)
