package com.antolab.muscular.utils

import android.app.*
import android.content.Context
import android.os.*
import android.util.*
import android.view.*
import android.widget.*
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.antolab.muscular.MyApplication
import com.antolab.muscular.db.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.io.*


class PrePopulation(private val context: Context, private var appDao: AppDao) {

    init {
        val database = MyApplication.appDatabase
        appDao = database.appDao()
    }

    suspend fun exercisesPrepopulation() {
        withContext(Dispatchers.IO) {
            val dbPath = "exercises.json"
            val oldDb: MutableMap<String, ExerciseWithTranslations> = readJsonFromFileExercise(dbPath)
            Log.d("prepopulation", "json DB loading: ${oldDb.toString()}")
            for (exerciseEntry in oldDb) {
                val exercise = exerciseEntry.value
                val exerciseEntity = ExerciseEntity(
                    id = 0,
                    name = exercise.name_it,
                    description = exercise.description_en,
                    image = exercise.image,
                    language = "it",  // Default language, you might want to change this
                    name_en = exercise.name_en,
                    name_es = exercise.name_es,
                    name_de = exercise.name_de,
                    name_fr = exercise.name_fr,
                    name_it = exercise.name_it,
                    description_en = exercise.description_en,
                    description_es = exercise.description_es,
                    description_de = exercise.description_de,
                    description_fr = exercise.description_fr,
                    description_it = exercise.description_it
                )
                appDao.insertExercise(exerciseEntity)
            }
            val allExercises = appDao.getAllExercises()
            Log.d("prepopulation", allExercises.toString())
        }
    }

    data class ExerciseWithTranslations(
        @SerializedName("id") val id: Long,
        @SerializedName("name_en") val name_en: String,
        @SerializedName("name_es") val name_es: String,
        @SerializedName("name_de") val name_de: String,
        @SerializedName("name_fr") val name_fr: String,
        @SerializedName("name_it") val name_it: String,
        @SerializedName("description_en") val description_en: String,
        @SerializedName("description_es") val description_es: String,
        @SerializedName("description_de") val description_de: String,
        @SerializedName("description_fr") val description_fr: String,
        @SerializedName("description_it") val description_it: String,
        @SerializedName("image") val image: String
    )

    private fun readJsonFromFileExercise(fileName: String): MutableMap<String, ExerciseWithTranslations> {
        val jsonString = StringBuilder()
        try {
            // Open the file input stream
            context.assets.open(fileName).use { inputStream ->
                // Create a buffered reader
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    // Read the file line by line
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        jsonString.append(line)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Gson().fromJson(
            jsonString.toString(),
            object : TypeToken<MutableMap<String, ExerciseWithTranslations>>() {}.type
        ) ?: mutableMapOf()
    }

    suspend fun setPrepopulation() {
        withContext(Dispatchers.IO) {
            val dbPath = "sets.json"
            val oldDb : MutableMap<String, Set> = readJsonFromFileSet(dbPath)
            Log.d("prepopulation", "json DB loading: ${oldDb.toString()}")
            for (setEntry in oldDb) {
                val set = setEntry.value
                val setEntity = SetEntity(
                    id = 0,
                    exerciseId = set.exerciseId,
                    reps = set.reps,
                    weight = set.weight
                )
                appDao.insertSet(setEntity)
            }
            val allSets = appDao.getAllSets()
            Log.d("prepopulation", allSets.toString())
        }
    }

    data class Set(
        @SerializedName("id") val id: Long,
        @SerializedName("exerciseId") val exerciseId: Long,
        @SerializedName("reps") val reps: Int,
        @SerializedName("weight") val weight: Int
    )

    private fun readJsonFromFileSet(fileName: String): MutableMap<String, Set> {
        val jsonString = StringBuilder()
        try {
            // Open the file input stream
            context.assets.open(fileName).use { inputStream ->
                // Create a buffered reader
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    // Read the file line by line
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        jsonString.append(line)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Gson().fromJson(jsonString.toString(), object : TypeToken<MutableMap<String, Set>>() {}.type)
            ?: mutableMapOf()
    }

    suspend fun pePrepopulation() {
        val tag="prepopulation"
        withContext(Dispatchers.IO) {
            val dbPath = "pe.json"
            val oldDb : MutableMap<String, PE> = readJsonFromFilePE(dbPath)
            Log.d(tag, "json DB loading: ${oldDb.toString()}")
            for (peEntry in oldDb) {
                val pe = peEntry.value
                Log.d(tag, "Trying to add $pe")
                val peEntity = PeEntity(
                    programmeId = pe.programmeId,
                    exerciseId = pe.exerciseId,
                    restTimer = pe.restTimer
                )
                appDao.insertPE(peEntity)
            }
            val rel = appDao.getRelationshipsCount()
            Log.d(tag, rel.toString())
        }
    }

    data class PE(
        @SerializedName("programmeId") val programmeId: String,
        @SerializedName("exerciseId") val exerciseId: Long,
        @SerializedName("restTimer") val restTimer: Long
    )

    private fun readJsonFromFilePE(fileName: String): MutableMap<String, PE> {
        val jsonString = StringBuilder()
        try {
            // Open the file input stream
            context.assets.open(fileName).use { inputStream ->
                // Create a buffered reader
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    // Read the file line by line
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        jsonString.append(line)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Gson().fromJson(jsonString.toString(), object : TypeToken<MutableMap<String, PE>>() {}.type)
            ?: mutableMapOf()
    }

    data class Programme(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        @ColumnInfo(name = "name_en") val name_en: String,
        @ColumnInfo(name = "name_es") val name_es: String,
        @ColumnInfo(name = "name_fr") val name_fr: String,
        @ColumnInfo(name = "name_de") val name_de: String,
        @ColumnInfo(name = "name_it") val name_it: String
        // Add other columns as needed
    )

    suspend fun programmesPrepopulation() {
        withContext(Dispatchers.IO) {
            val dbPath = "programmes.json"
            val oldDb: MutableMap<String, Programme> = readJsonFromFileProgramme(dbPath)
            Log.d("prepopulation", "json DB loading: ${oldDb.toString()}")
            for (programmeEntry in oldDb) {
                val programme = programmeEntry.value
                Log.d("prepopulation", "Trying to add $programme")
                val programmeEntity = ProgrammeEntity(
                name_en = programme.name_en,
                name_es = programme.name_es,
                name_de = programme.name_de,
                name_fr = programme.name_fr,
                name_it = programme.name_it
                )
                appDao.insertProgramme(programmeEntity)

            }
            val allProgrammes = appDao.getAllProgrammes()
            Log.d("prepopulation", allProgrammes.toString())
        }
    }

    private fun readJsonFromFileProgramme(fileName: String): MutableMap<String, Programme> {
        val jsonString = StringBuilder()
        try {
            // Open the file input stream
            context.assets.open(fileName).use { inputStream ->
                // Create a buffered reader
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    // Read the file line by line
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        jsonString.append(line)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Gson().fromJson(jsonString.toString(), object : TypeToken<MutableMap<String, Programme>>() {}.type)
            ?: mutableMapOf()
    }

}