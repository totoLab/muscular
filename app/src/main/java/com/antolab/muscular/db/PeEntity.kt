package com.antolab.muscular.db

import androidx.room.*
import com.antolab.muscular.utils.*
import kotlin.time.Duration

@Entity(
    tableName = "pe",
    foreignKeys = [
        ForeignKey(
            entity = ProgrammeEntity::class,
            parentColumns = ["name"],
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
    val restTimer: Int // TODO use TypeConverters in utils.Converters
)
