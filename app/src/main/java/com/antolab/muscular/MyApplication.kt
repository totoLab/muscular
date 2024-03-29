package com.antolab.muscular

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.room.Room
import com.antolab.muscular.db.AppDao
import com.antolab.muscular.db.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApplication : Application() {

    companion object {
        lateinit var appDatabase: AppDatabase
        lateinit var appDao: AppDao
        lateinit var sharedPreferences: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()

        try {
            sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val defaultLanguage = sharedPreferences.getString("selectedLanguage", "en") ?: "en"

            appDatabase = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).build()

            appDao = appDatabase.appDao()

            GlobalScope.launch {
                appDao.updateLanguagePreference(defaultLanguage)
            }
        } catch (e: Exception) {
            Log.e("RoomDatabase", "Error creating database", e)
        }

    }

}
