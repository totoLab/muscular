package com.antolab.muscular.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ExerciseEntity::class, PeEntity::class, ProgrammeEntity::class, SetEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
