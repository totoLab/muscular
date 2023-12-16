package com.antolab.muscular.db

import androidx.room.*

@Database(entities = [ExerciseEntity::class, ProgrammeEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
