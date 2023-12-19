package com.antolab.muscular.utils

import android.app.*
import android.os.*
import android.util.*
import android.view.*
import android.widget.*
import com.antolab.muscular.db.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.io.*
import android.content.Context
import com.antolab.muscular.MyApplication

class PrePopulation(private val context: Context) {

    private val appDao: AppDao

    init {
        val database = MyApplication.appDatabase
        appDao = database.appDao()
    }

    suspend fun exercisesPrepopulation() {
        withContext(Dispatchers.IO) {
            val dbPath = "exercises.json"
            val oldDb : MutableMap<String, Exercise> = readJsonFromFileExercise(dbPath)
            Log.d("prepopulation", "json DB loading: ${oldDb.toString()}")
            for (exerciseEntry in oldDb) {
                var exercise = exerciseEntry.value
                var exerciseEntity = ExerciseEntity(
                    id = 0,
                    name = exercise.name,
                    description = exercise.description,
                    image = exercise.name
                )
                appDao.insertExercise(exerciseEntity)
            }
            val allExercises = appDao.getAllExercises()
            Log.d("prepopulation", allExercises.toString())
        }
    }

    data class Exercise(
        @SerializedName("name") val name: String,
        @SerializedName("description") val description: String,
        @SerializedName("image") val image: String
    )

    private fun readJsonFromFileExercise(fileName: String): MutableMap<String, Exercise> {
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
        return Gson().fromJson(jsonString.toString(), object : TypeToken<MutableMap<String, Exercise>>() {}.type)
            ?: mutableMapOf()
    }

    suspend fun programmesPrepopulation() {
        withContext(Dispatchers.IO) {
            val dbPath = "programmes.json"
            val oldDb : MutableMap<String, Programme> = readJsonFromFileProgramme(dbPath)
            Log.d("prepopulation", "json DB loading: ${oldDb.toString()}")
            for (programmeEntry in oldDb) {
                var programme = programmeEntry.value
                Log.d("prepopulation", "Trying to add $programme")
                var programmeEntity = ProgrammeEntity(
                    name = programme.name
                )
                appDao.insertProgramme(programmeEntity)
            }
            val allProgrammes = appDao.getAllProgrammes()
            Log.d("prepopulation", allProgrammes.toString())
        }
    }

    data class Programme(
        @SerializedName("name") val name: String,
    )

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

    suspend fun setPrepopulation() {
        withContext(Dispatchers.IO) {
            val dbPath = "sets.json"
            val oldDb : MutableMap<String, Set> = readJsonFromFileSet(dbPath)
            Log.d("prepopulation", "json DB loading: ${oldDb.toString()}")
            for (setEntry in oldDb) {
                var set = setEntry.value
                var setEntity = SetEntity(
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
                var pe = peEntry.value
                Log.d(tag, "Trying to add $pe")
                var peEntity : PeEntity = PeEntity(
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
}