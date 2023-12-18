package com.antolab.muscular.db

import androidx.room.*

@Entity(
    tableName = "sett",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )],
    primaryKeys = [ "id", "exerciseId" ]
    )
data class SetEntity(
    val id: Long,
    val exerciseId: Long,
    val reps: Int,
    val weight: Int
)
