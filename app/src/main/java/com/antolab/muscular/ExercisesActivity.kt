package com.antolab.muscular

import android.os.*
import android.util.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.util.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

class ExercisesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        val scrollView = findViewById<ScrollView>(R.id.scrollView) ?: return // TODO remove unused
        val container = findViewById<LinearLayout>(R.id.container) ?: return

        // add exercises dynamically
        val exercisesDb = readJsonFromFile("exercises.json")
        if (exercisesDb.isEmpty()) {
            val empty = findViewById<TextView>(R.id.exercices_default_empty)
            empty.visibility = View.VISIBLE;
            return
        } else {
            for (exerciseEntry in exercisesDb) {
                val adding_outcome: Boolean = showExercise(container, exerciseEntry.value)
                Log.d("exerciseLoading", "$exerciseEntry was ${if (adding_outcome) "" else "not"} added to the scroll view")
            }
        }
    }

    private fun showExercise(container: LinearLayout, exercise: Exercise): Boolean {
        val exerciseTemplateLayoutId = R.layout.exercise_template

        // Inflate the exercise template and make it visible
        val exerciseElement : LinearLayout = layoutInflater.inflate(exerciseTemplateLayoutId, null) as LinearLayout
        exerciseElement.visibility = View.VISIBLE

        // Setup information about the exercise
        // name
        val textViewExerciseName = exerciseElement.findViewById<TextView>(R.id.exerciseName)
        textViewExerciseName.text = exercise.name

        // description
        val textViewExerciseDescription = exerciseElement.findViewById<TextView>(R.id.exerciseDescription)
        textViewExerciseDescription.text = exercise.description

        // image
        setImage(exerciseElement, exercise.image)

        // deleting specific exercise from database
        val deleteButton = exerciseElement.findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            // TODO: Implement delete functionality
            Toast.makeText(this, "Deleted ${exercise.name} from the list", Toast.LENGTH_LONG).show()
        }

        // add to the container
        container.addView(exerciseElement)
        return true // TODO: implement error reporting
    }

    private fun readJsonFromFile(fileName: String): Map<String, Exercise> {
        val jsonString = StringBuilder()
        try {
            // Open the file input stream
            applicationContext.assets.open(fileName).use { inputStream ->
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
        return Gson().fromJson(jsonString.toString(), object : TypeToken<Map<String, Exercise>>() {}.type)
            ?: emptyMap()
    }

    // TODO: If you're calling the setImage() function from the main thread, it might cause the UI thread to freeze while loading the image.
    //  Consider using an asynchronous task or background thread to load the image without blocking the main UI.
    private fun setImage(inflatedElement: LinearLayout, imageName: String) {
        val id: Int = imageId(imageName) // assuming image resources are saved in res/drawable
        val image = inflatedElement.findViewById<ImageView>(R.id.exerciseImage)
        if (id == 0) {
            image.visibility = View.GONE
            return
        } else {
            image.setImageResource(id)
        }
        Log.d("imageLoading", "Image $id is from $imageName")
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
