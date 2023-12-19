package com.antolab.muscular.db

import androidx.room.*

@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPE(peEntity: PeEntity)

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

    @Query("""
        SELECT COUNT(Ex.id)
        FROM exercises AS Ex
        WHERE Ex.id IN (
            SELECT exerciseId
            FROM pe
            WHERE programmeId = :programmeName
            )
    """)
    suspend fun getExerciseOfProgrammeCount(programmeName: String): Int

    @Query("SELECT * FROM programme WHERE name = :programmeId")
    suspend fun getProgramme(programmeId: String): ProgrammeEntity?
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExercise(exerciseId: Long): ExerciseEntity?

    @Query("SELECT COUNT(*) FROM pe")
    suspend fun getRelationshipsCount(): Int

    @Delete
    suspend fun deletePE(programmeEntity: ProgrammeEntity, exerciseEntity: ExerciseEntity)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSet(setEntity: SetEntity)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun deleteSet(setEntity: SetEntity)

    @Query("SELECT * FROM sett")
    suspend fun getAllSets() : List<SetEntity>

    @Query("SELECT * FROM sett WHERE exerciseId = :exerciseId")
    suspend fun getAllSetsOfExercise(exerciseId: Long) : List<SetEntity>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProgramme(programme: ProgrammeEntity)

    @Query("SELECT * FROM programme")
    suspend fun getAllProgrammes(): List<ProgrammeEntity>

    @Query("SELECT COUNT(*) FROM programme")
    suspend fun getProgrammesCount(): Int

    @Delete
    suspend fun deleteProgramme(programme: ProgrammeEntity)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExercisesCount(): Int

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)
}
