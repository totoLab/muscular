package com.antolab.muscular

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.antolab.muscular.db.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class WorkoutActivity : AppCompatActivity() {
    private lateinit var appDao: AppDao
    private lateinit var currentProgramme: String
    private lateinit var selectedLanguage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)


        // Load selected language from SharedPreferences
        selectedLanguage = loadLocate()

        val intent = intent
        if (intent == null) {
            finish()
            Toast.makeText(this, "ERROR: no programme was passed", Toast.LENGTH_LONG).show()
        } else {
            currentProgramme = intent.getStringExtra("programmeName").toString()
            Log.d("WorkoutActivity", "Current Programme: $currentProgramme")
            val database = MyApplication.appDatabase
            appDao = database.appDao()
        }

    }

    override fun onStart() {
        super.onStart()

        val container = findViewById<LinearLayout>(R.id.workout_exercises_container) ?: return

        MainScope().launch {
            Log.d("WorkoutActivity", "MainScope launch block entered")
            val exerciseCount = appDao.getExerciseOfProgrammeCount(currentProgramme)
            Log.d("WorkoutActivity", "Exercise Count: $exerciseCount")
            if (appDao.getExerciseOfProgrammeCount(currentProgramme) >= 1) {
                // add exercises dynamically
                for (exercise in appDao.getAllExerciseOfProgramme(currentProgramme)) {
                    Log.d("WorkoutActivity", "Adding exercise: $exercise")
                    val adding_outcome: Boolean = showExercise(container, exercise)
                    Log.d("exerciseProgrammeLoading", "$exercise was ${if (adding_outcome) "" else "not"} added to the scroll view")
                }
            }
        }

    }

    private suspend fun showExercise(container: LinearLayout, exercise: ExerciseEntity): Boolean {
        // Inflate the exercise template and make it visible
        val exerciseWrapper : ConstraintLayout= layoutInflater.inflate(R.layout.exercise_instance_wrapper, null) as ConstraintLayout

        // title
        val title : TextView = exerciseWrapper.findViewById(R.id.exercise_name)
        title.text = getLocalizedExerciseName(exercise)

        // sets
        val exerciseElement : TableLayout = exerciseWrapper.findViewById(R.id.exercise_instance)
        val sets : List<SetEntity> = appDao.getAllSetsOfExercise(exercise.id)


        val labelReps = resources.getString(R.string.curr_reps)
        val labelWeight = resources.getString(R.string.curr_weight)

        showSet(exerciseElement, "ID", labelReps, labelWeight)

        for (set in sets) {
            showSet(exerciseElement, set.id.toString(), set.reps.toString(), set.weight.toString())
        }

        container.addView(exerciseWrapper)
        return true
    }

    private fun showSet(exerciseElement: TableLayout, id: String, reps: String, weight: String) {
        val setElement : TableRow = layoutInflater.inflate(R.layout.set_template, null) as TableRow

        val idView = setElement.findViewById<TextView>(R.id.id_number)
        idView.text = id

        val repsView = setElement.findViewById<TextView>(R.id.reps_number)
        repsView.text = reps

        val weightView = setElement.findViewById<TextView>(R.id.weight_number)
        weightView.text = weight

        exerciseElement.addView(setElement)
    }

    private fun getLocalizedExerciseName(exercise: ExerciseEntity): String {
        return when (selectedLanguage) {
            "en" -> exercise.name_en
            "es" -> exercise.name_es
            "de" -> exercise.name_de
            "fr" -> exercise.name_fr
            "it" -> exercise.name_it
            else -> exercise.name_en  // Fallback to default name
        }
    }

    private fun loadLocate(): String {
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("My_Lang", "en") ?: "en"
    }
}

