package com.antolab.muscular

import android.app.*
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
    private lateinit var appDatabase: AppDatabase
    private lateinit var appDao: AppDao
    private var TESTING = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        try {
            appDatabase = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).build()

            appDao = appDatabase.appDao()

            val button_add : Button = findViewById(R.id.button_exercise_new)
            button_add.setOnClickListener {
                MainScope().launch {
                    if (appDao.getCount() == 0) prepopulation()
                    else Toast.makeText(this@ExercisesActivity, "DB is not empty", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("RoomDatabase", "Error creating database", e)
        }
    }

    override fun onStart() {
        super.onStart()

        val container = findViewById<LinearLayout>(R.id.container) ?: return

        MainScope().launch {
            if (appDao.getCount() == 0) {
                val empty = findViewById<TextView>(R.id.exercices_default_empty)
                empty.visibility = View.VISIBLE;
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
                appDao.delete(exercise)
                Log.d("exerciseDeletion", "Deleted $exercise from the list.}")
            }
        }

        container.addView(exerciseElement)
        return setImageOutcome
    }

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

    private suspend fun prepopulation() {
        withContext(Dispatchers.IO) {
            val dbPath = "exercises.json"
            val oldDb : MutableMap<String, Exercise> = readJsonFromFile(dbPath)
            Log.d("prepopulation", "json DB loading: ${oldDb.toString()}")
            for (exerciseEntry in oldDb) {
                var exercise = exerciseEntry.value
                var exerciseEntity = ExerciseEntity(
                    name = exercise.name,
                    description = exercise.description,
                    image = exercise.name
                )
                appDao.insert(exerciseEntity)
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

}
