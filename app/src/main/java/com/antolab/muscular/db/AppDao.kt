package com.antolab.muscular.db

import androidx.room.*

@Dao
interface AppDao {

    @Insert
    suspend fun insertPE(programme: ProgrammeEntity, exercise: ExerciseEntity)

    @Query("""
        SELECT Ex.*
        FROM exercises AS Ex
        WHERE Ex.id IN (
            SELECT exerciseId
            FROM pe
            WHERE programmeId = :programmeName
            )
    """)
    suspend fun getAllExerciseOfProgramme(programmeName: String): List<ExerciseEntity>

    @Query("SELECT COUNT(*) FROM pe")
    suspend fun getRelationshipsCount(): Int

    @Delete
    suspend fun deletePE(programmeEntity: ProgrammeEntity, exerciseEntity: ExerciseEntity)

    @Insert
    suspend fun insertProgramme(programme: ProgrammeEntity)

    @Query("SELECT * FROM programme")
    suspend fun getAllProgrammes(): List<ProgrammeEntity>

    @Query("SELECT COUNT(*) FROM programme")
    suspend fun getProgrammesCount(): Int

    @Delete
    suspend fun deleteProgramme(programme: ProgrammeEntity)

    @Insert
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExercisesCount(): Int

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)
}
