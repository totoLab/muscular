package com.antolab.muscular.db

import androidx.room.*

@Entity(tableName = "pe")
data class PeEntity(
    @PrimaryKey
    val programmeId: Int,
    val exerciseId: Int,
    val lastWeight: Int,
    val lastReps: Int,
    val restTimer: Int // TODO use Duration with TypeConverters in utils.Converters
)
