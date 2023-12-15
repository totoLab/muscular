package com.antolab.muscular

import android.app.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.util.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.*
import java.util.*
import androidx.room.*
import kotlinx.coroutines.*
import com.antolab.muscular.db.*

class ExercisesActivity : AppCompatActivity() {
    private val dbPath = "exercises.json"
    private lateinit var exerciseDatabase: ExerciseDatabase
    private lateinit var exerciseDao: ExerciseDao
    private var TESTING = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        try {
            exerciseDatabase = Room.databaseBuilder(
                applicationContext,
                ExerciseDatabase::class.java,
                "exercise_database"
            ).build()

            exerciseDao = exerciseDatabase.exerciseDao()
            MainScope().launch {
                if (TESTING && exerciseDao.getCount() == 0) prepopulation()
            }
        } catch (e: Exception) {
            Log.e("RoomDatabase", "Error creating database", e)
        }
    }

    override fun onStart() {
        super.onStart()

        val container = findViewById<LinearLayout>(R.id.container) ?: return

        MainScope().launch {
            if (exerciseDao.getCount() == 0) {
                val empty = findViewById<TextView>(R.id.exercices_default_empty)
                empty.visibility = View.VISIBLE;
                return@launch
            } else {
                // add exercises dynamically
                for (exercise in exerciseDao.getAllExercises()) {
                    val adding_outcome: Boolean = showExercise(container, exercise)
                    Log.d("exerciseLoading", "$exercise was ${if (adding_outcome) "" else "not"} added to the scroll view")
                }
            }
        }
    }

    private fun showExercise(container: LinearLayout, exercise: ExerciseEntity): Boolean {
        // Inflate the exercise template and make it visible
        val exerciseElement : RelativeLayout = layoutInflater.inflate(R.layout.exercise_template, null) as RelativeLayout
        exerciseElement.visibility = View.VISIBLE

        // Setup information about the exercise
        // name
        val textViewExerciseName = exerciseElement.findViewById<TextView>(R.id.exerciseName)
        textViewExerciseName.text = exercise.name

        // description
        val textViewExerciseDescription = exerciseElement.findViewById<TextView>(R.id.exerciseDescription)
        textViewExerciseDescription.text = exercise.description

        // image
        val setImageOutcome = setImage(exerciseElement, exercise.image)

        // deleting specific exercise from database
        val deleteButton = exerciseElement.findViewById<Button>(R.id.exercise_delete_button)
        deleteButton.setOnClickListener {
            container.removeView(exerciseElement)
            MainScope().launch {
                exerciseDao.delete(exercise)
                Log.d("exerciseDeletion", "Deleted $exercise from the list.}")
            }
        }

        container.addView(exerciseElement)
        return setImageOutcome
    }

    private suspend fun prepopulation() {
        withContext(Dispatchers.IO) {
            val oldDb : MutableMap<String, Exercise> = readJsonFromFile(dbPath)
            Log.d("prepopulation", "json DB loading: ${oldDb.toString()}")
            for (exerciseEntry in oldDb) {
                var exercise = exerciseEntry.value
                var exerciseEntity = ExerciseEntity(
                    name = exercise.name,
                    description = exercise.description,
                    image = exercise.name
                )
                exerciseDao.insert(exerciseEntity)
            }
            val allExercises = exerciseDao.getAllExercises()
            Log.d("prepopulation", allExercises.toString())
        }
    }

    private fun readJsonFromFile(fileName: String): MutableMap<String, Exercise> {
        val jsonString = StringBuilder()
        try {
            // Open the file input stream
            assets.open(fileName).use { inputStream ->
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

    // TODO: If you're calling the setImage() function from the main thread, it might cause the UI thread to freeze while loading the image.
    //  Consider using an asynchronous task or background thread to load the image without blocking the main UI.
    private fun setImage(inflatedElement: RelativeLayout, imageName: String): Boolean {
        var outcome: Boolean
        var msg: String

        val id: Int = imageId(imageName) // assuming image resources are saved in res/drawable
        val imageThumbButton = inflatedElement.findViewById<ImageView>(R.id.image_thumb_button)
        if (id == 0) {
            imageThumbButton.visibility = View.GONE
            msg = "Image $id not loaded from $imageName"
            outcome = false
        } else {
            imageThumbButton.setImageResource(id)
            imageThumbButton.setOnClickListener{
                showFullscreenImage(null, id)
            }
            msg = "Image $id loaded from $imageName"
            outcome = true
        }
        Log.d("imageLoading", msg)
        return outcome
    }


    fun showFullscreenImage(view: View?, imageId: Int) {
        // Create a dialog with a custom layout
        val fullscreenDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        fullscreenDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        fullscreenDialog.setContentView(R.layout.dialog_fullscreen_image)

        val fullscreenImageView = fullscreenDialog.findViewById<ImageView>(R.id.fullscreenImageView)
        fullscreenImageView.setImageResource(imageId)
        fullscreenDialog.show()
    }

    private fun imageId(imagePath: String) : Int {
        return resources.getIdentifier(imagePath, "drawable", packageName)
    }

    data class Exercise(
        @SerializedName("name") val name: String,
        @SerializedName("description") val description: String,
        @SerializedName("image") val image: String
    )

}
