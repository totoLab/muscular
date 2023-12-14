package com.antolab.muscular

import android.app.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.util.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.*
import java.util.*
import android.Manifest

class ExercisesActivity : AppCompatActivity() {
    private val dbPath = "exercises.json"
    private lateinit var exercisesDb : MutableMap<String, Exercise> // TODO needs to be unmutable, not var
    private var permissionsGranted = false
    private val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)
        val scrollView = findViewById<ScrollView>(R.id.scrollView) ?: return // TODO remove unused
        val container = findViewById<LinearLayout>(R.id.container) ?: return

        exercisesDb = readJsonFromFile(dbPath)
        Log.d("recreationDB", "DB at activity creation: ${exercisesDb.toString()}")

        if (!checkWriteStoragePermission()) permissionsGranted = true

        // add exercises dynamically
        if (exercisesDb.isEmpty()) {
            val empty = findViewById<TextView>(R.id.exercices_default_empty)
            empty.visibility = View.VISIBLE;
            return
        } else {
            for (exerciseEntry in exercisesDb) {
                val adding_outcome: Boolean = showExercise(container, exerciseEntry)
                Log.d("exerciseLoading", "$exerciseEntry was ${if (adding_outcome) "" else "not"} added to the scroll view")
            }
        }
    }

    private fun showExercise(container: LinearLayout, entry: Map.Entry<String, Exercise>): Boolean {
        val exerciseId = entry.key
        val exercise = entry.value
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
            exercisesDb.remove(exerciseId)
            writeMapToJson(dbPath, exercisesDb)
            Log.d("exerciseDeletion", "Deleted ${exercise.name} from the list. New Map:\n ${exercisesDb.toString()}")
        }

        // add to the container
        container.addView(exerciseElement)
        return setImageOutcome
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

    private fun writeMapToJson(fileName: String, map: MutableMap<String, Exercise>) {
        try {
            val jsonString = Gson().toJson(map)

            // Open the file output stream
            applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
                // Write the JSON string to the file
                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                    writer.write(jsonString)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
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

    fun checkWriteStoragePermission(): Boolean {
        // check if the permission was already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )
            return false // permission is not yet granted
        } else {
            return true // permission is already granted
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, do nothing here, handle in the calling code
                } else {
                    // Permission denied, handle accordingly (e.g., show a message to the user)
                }
                // Call a method or set a flag to handle the result in the calling code
            }
            // Add other permission request cases if needed
        }
    }

}
