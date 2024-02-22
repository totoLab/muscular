package com.antolab.muscular

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.antolab.muscular.db.AppDao
import com.antolab.muscular.db.ExerciseEntity
import com.antolab.muscular.utils.PrePopulation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ExercisesActivity : AppCompatActivity() {
    private lateinit var appDao: AppDao
    private lateinit var selectedLanguage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        val database = MyApplication.appDatabase
        appDao = database.appDao()

        // Load language from SharedPreferences
        selectedLanguage = loadLocate()

        val buttonAdd: Button = findViewById(R.id.button_exercise_new)
        buttonAdd.setOnClickListener {
            GlobalScope.launch {
                if (appDao.getExercisesCount() == 0) {
                    val instance = PrePopulation(this@ExercisesActivity, appDao)
                    instance.exercisesPrepopulation()
                    instance.setPrepopulation()
                } else {
                    // Handle if DB is not empty
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()

        val container = findViewById<LinearLayout>(R.id.container) ?: return

        MainScope().launch {
            if (appDao.getExercisesCount() == 0) {
                val empty = findViewById<TextView>(R.id.exercises_default_empty)
                empty.visibility = View.VISIBLE
                return@launch
            } else {
                // add exercises dynamically
                for (exercise in appDao.getAllExercises()) {
                    val adding_outcome: Boolean = showExercise(container, exercise)
                    Log.d("exerciseLoading", "$exercise was ${if (adding_outcome) "" else "not"} added to the scroll view")
                }
            }
        }
    }

    private fun showExercise(container: LinearLayout, exercise: ExerciseEntity): Boolean {
        // Inflate the exercise template and make it visible
        val exerciseElement: RelativeLayout = layoutInflater.inflate(R.layout.exercise_template, null) as RelativeLayout
        exerciseElement.visibility = View.VISIBLE

        // Setup information about the exercise
        // name
        val textViewExerciseName = exerciseElement.findViewById<TextView>(R.id.exerciseName)
        val exerciseName = when (selectedLanguage) {
            "en" -> exercise.name_en
            "es" -> exercise.name_es
            "de" -> exercise.name_de
            "fr" -> exercise.name_fr
            "it" -> exercise.name_it
            else -> exercise.name  // Fallback to default name
        }

        textViewExerciseName.text = exerciseName

        // description
        val textViewExerciseDescription = exerciseElement.findViewById<TextView>(R.id.exerciseDescription)
        val exerciseDescription = when (selectedLanguage) {
            "en" -> exercise.description_en
            "es" -> exercise.description_es
            "de" -> exercise.description_de
            "fr" -> exercise.description_fr
            "it" -> exercise.description_it
            else -> exercise.description  // Fallback to default description
        }
        textViewExerciseDescription.text = exerciseDescription

        // image
        val setImageOutcome = setImage(exerciseElement, exercise.image)

        // deleting specific exercise from the database
        val deleteButton = exerciseElement.findViewById<Button>(R.id.exercise_delete_button)
        deleteButton.setOnClickListener {
            container.removeView(exerciseElement)
            MainScope().launch {
                appDao.deleteExercise(exercise)
                Log.d("exerciseDeletion", "Deleted $exercise from the list.}")
            }
        }

        container.addView(exerciseElement)
        return setImageOutcome
    }




    private fun setImage(inflatedElement: RelativeLayout, imageName: String): Boolean {
        val outcome: Boolean
        val msg: String

        val id: Int = imageId(imageName) // assuming image resources are saved in res/drawable
        val imageThumbButton = inflatedElement.findViewById<ImageView>(R.id.image_thumb_button)
        if (id == 0) {
            imageThumbButton.visibility = View.GONE
            msg = "Image $id not loaded from $imageName"
            outcome = false
        } else {
            imageThumbButton.setImageResource(id)
            imageThumbButton.setOnClickListener{
                showFullscreenImage(id)
            }
            msg = "Image $id loaded from $imageName"
            outcome = true
        }
        Log.d("imageLoading", msg)
        return outcome
    }

    fun showFullscreenImage(imageId: Int) {
        val fullscreenDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        fullscreenDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        fullscreenDialog.setContentView(R.layout.dialog_fullscreen_image)

        val fullscreenImageView = fullscreenDialog.findViewById<ImageView>(R.id.fullscreenImageView)
        fullscreenImageView.setImageResource(imageId)
        fullscreenDialog.show()
    }

    @SuppressLint("DiscouragedApi")
    private fun imageId(imagePath: String) : Int {
        return resources.getIdentifier(imagePath, "drawable", packageName)
    }
    private fun loadLocate(): String {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        return sharedPreferences.getString("My_Lang", "it") ?: "it"
    }
}
