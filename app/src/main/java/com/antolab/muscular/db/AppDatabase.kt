package com.antolab.muscular.db

import androidx.room.*

@Database(entities = [ExerciseEntity::class, PeEntity::class, ProgrammeEntity::class, SetEntity::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
