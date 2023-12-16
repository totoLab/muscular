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
            for (exerciseEntry in oldDb) {
                var exercise = exerciseEntry.value
                var exerciseEntity = ProgrammeEntity(
                    name = exercise.name
                )
                appDao.insertProgramme(exerciseEntity)
            }
            val allExercises = appDao.getAllProgrammes()
            Log.d("prepopulation", allExercises.toString())
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
}