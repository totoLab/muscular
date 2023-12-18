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
    private lateinit var appDao : AppDao
    private lateinit var currentProgramme : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        val intent = intent
        if (intent == null) {
            finish()
            Toast.makeText(this, "ERROR: no programme was passed", Toast.LENGTH_LONG).show()
        } else {
            currentProgramme = intent.getStringExtra("programmeName").toString()
            val database = MyApplication.appDatabase
            appDao = database.appDao()
        }


    }

    override fun onStart() {
        super.onStart()

        val container = findViewById<LinearLayout>(R.id.workout_exercises_container) ?: return

        MainScope().launch {
            if (appDao.getExerciseOfProgrammeCount(currentProgramme) >= 1) {
                // add exercises dynamically
                for (exercise in appDao.getAllExerciseOfProgramme(currentProgramme)) {
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
        title.text = exercise.name

        // sets
        val exerciseElement : TableLayout = exerciseWrapper.findViewById(R.id.exercise_instance)
        val sets : List<SetEntity> = appDao.getAllSetsOfExercise(exercise.id)
        for (set in sets) {
            val setElement : TableRow = layoutInflater.inflate(R.layout.set_template, null) as TableRow

            val idView = setElement.findViewById<TextView>(R.id.id_number)
            idView.text = set.id.toString()

            val repsView = setElement.findViewById<TextView>(R.id.id_number)
            repsView.text = set.reps.toString()

            val weightView = setElement.findViewById<TextView>(R.id.id_number)
            weightView.text = set.weight.toString()

            exerciseElement.addView(setElement)
        }

        container.addView(exerciseWrapper)
        return true
    }
}