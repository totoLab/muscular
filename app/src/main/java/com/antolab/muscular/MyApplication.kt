package com.antolab.muscular

import android.app.*
import android.util.Log
import androidx.room.*
import com.antolab.muscular.db.*

class MyApplication : Application() {
    private lateinit var appDao: AppDao

    companion object {
        lateinit var appDatabase: AppDatabase
    }
    override fun onCreate() {
        super.onCreate()

        try {
            appDatabase = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).build()

        } catch (e: Exception) {
            Log.e("RoomDatabase", "Error creating database", e)
        }

        appDao = appDatabase.appDao()
    }

}