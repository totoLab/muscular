package com.antolab.muscular.db

import androidx.room.*

@Dao
interface AppDao {

    @Insert
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExercisesCount(): Int

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)
}
