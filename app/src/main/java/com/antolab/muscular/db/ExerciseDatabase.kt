package com.antolab.muscular.db

import androidx.room.*

@Database(entities = [ExerciseEntity::class], version = 1)
abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
}
