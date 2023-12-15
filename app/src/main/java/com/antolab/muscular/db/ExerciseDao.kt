package com.antolab.muscular.db

import androidx.room.*

@Dao
interface ExerciseDao {

    @Insert
    suspend fun insert(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getCount(): Int

    @Delete
    suspend fun delete(exercise: ExerciseEntity)
}
