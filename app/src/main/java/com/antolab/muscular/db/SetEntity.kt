package com.antolab.muscular.db

import androidx.room.*

@Entity(
    tableName = "sett",
    indices = [
        Index(value = ["id", "exerciseId"], unique = true),
        Index(value = ["exerciseId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseId: Long,
    val reps: Int,
    val weight: Int
)
