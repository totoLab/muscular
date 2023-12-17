package com.antolab.muscular.db

import androidx.room.*

@Entity(
    tableName = "pe",
    foreignKeys = [
        ForeignKey(
            entity = ProgrammeEntity::class,
            parentColumns = ["name"],
            childColumns = ["programmeId"],
            onDelete = ForeignKey.CASCADE // Define the action on delete if needed
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PeEntity(
    @PrimaryKey val programmeId: String,
    val exerciseId: Int,
    val lastWeight: Int,
    val lastReps: Int,
    val restTimer: Int // TODO use Duration with TypeConverters in utils.Converters
)
