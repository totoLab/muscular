package com.antolab.muscular.db

import androidx.room.*

@Entity(
    tableName = "pe",
    foreignKeys = [
        ForeignKey(
            entity = ProgrammeEntity::class,
            parentColumns = ["name_it"],
            childColumns = ["programmeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = [ "programmeId", "exerciseId" ]
)
data class PeEntity(
    val programmeId: String,
    val exerciseId: Long,
    val restTimer: Long // TODO use TypeConverters in utils.Converters
)
