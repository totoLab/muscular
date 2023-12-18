package com.antolab.muscular.db

import androidx.room.*

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val description: String,
    val image: String
)
