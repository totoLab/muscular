package com.antolab.muscular

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.antolab.muscular.db.AppDao
import com.antolab.muscular.db.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Check if the 'language' column does not exist before adding it
        val cursor = db.compileStatement("PRAGMA table_info(exercises)").simpleQueryForLong()

        // Find the column index for the 'language' column
        val columnIndex = cursor.toInt()

        if (columnIndex == -1) {
            // 'language' column does not exist, add it
            db.execSQL("ALTER TABLE exercises ADD COLUMN language TEXT NOT NULL DEFAULT 'en'")
            // Add the new translation columns
            db.execSQL("ALTER TABLE exercises ADD COLUMN name_en TEXT DEFAULT 'undefined'")
            db.execSQL("ALTER TABLE exercises ADD COLUMN name_es TEXT DEFAULT 'undefined'")
            db.execSQL("ALTER TABLE exercises ADD COLUMN name_fr TEXT DEFAULT 'undefined'")
            db.execSQL("ALTER TABLE exercises ADD COLUMN name_de TEXT DEFAULT 'undefined'")
            db.execSQL("ALTER TABLE exercises ADD COLUMN name_it TEXT DEFAULT 'undefined'")

            db.execSQL("ALTER TABLE exercises ADD COLUMN description_es TEXT DEFAULT 'undefined'")
            db.execSQL("ALTER TABLE exercises ADD COLUMN description_it TEXT DEFAULT 'undefined'")
            db.execSQL("ALTER TABLE exercises ADD COLUMN description_en TEXT DEFAULT 'undefined'")
            db.execSQL("ALTER TABLE exercises ADD COLUMN description_fr TEXT DEFAULT 'undefined'")
            db.execSQL("ALTER TABLE exercises ADD COLUMN description_de TEXT DEFAULT 'undefined'")
        }
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create a new table with the desired schema
        db.execSQL("CREATE TABLE IF NOT EXISTS `programme_new` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`name_en` TEXT NOT NULL, " +
                "`name_es` TEXT NOT NULL, " +
                "`name_fr` TEXT NOT NULL, " +
                "`name_de` TEXT NOT NULL, " +
                "`name_it` TEXT NOT NULL, " +
                "PRIMARY KEY(`id`))")

        // Copy data from the old table to the new table
        db.execSQL("INSERT INTO `programme_new` " +
                "(`id`, `name`, `name_en`, `name_es`, `name_fr`, `name_de`, `name_it`) " +
                "SELECT `id`, `name`, `name_en`, `name_es`, `name_fr`, `name_de`, `name_it` FROM `programme`")

        // Remove the old table
        db.execSQL("DROP TABLE `programme`")

        // Rename the new table to the old table name
        db.execSQL("ALTER TABLE `programme_new` RENAME TO `programme`")
    }
}




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

            // Retrieve the selected language from shared preferences
            val defaultLanguage = sharedPreferences.getString("selectedLanguage", "en") ?: "en"

            appDatabase = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()

            appDao = appDatabase.appDao()

            // Set the selected language in the database
            GlobalScope.launch {
                appDao.updateLanguagePreference(defaultLanguage)
            }
        } catch (e: Exception) {
            Log.e("RoomDatabase", "Error creating database", e)
        }
    }
}
