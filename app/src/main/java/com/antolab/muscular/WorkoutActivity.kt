package com.antolab.muscular

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import android.os.CountDownTimer
import es.dmoral.toasty.Toasty






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

        val accelerometer = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    if (Math.abs(event.values[0]) > 10) {
                        // Il telefono è stato preso in mano
                        startTimer()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            }
        }
        accelerometer.registerListener(accelerometerListener, accelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }
    private var countdownTimer: CountDownTimer? = null
    private suspend fun showExercise(container: LinearLayout, exercise: ExerciseEntity): Boolean {
        // Inflate the exercise template and make it visible
        val exerciseWrapper : ConstraintLayout= layoutInflater.inflate(R.layout.exercise_instance_wrapper, null) as ConstraintLayout

        // title
        val title : TextView = exerciseWrapper.findViewById(R.id.exercise_name)
        title.text = exercise.name

        // sets
        val exerciseElement : TableLayout = exerciseWrapper.findViewById(R.id.exercise_instance)
        val sets : List<SetEntity> = appDao.getAllSetsOfExercise(exercise.id)

        showSet(exerciseElement, "ID", "Reps", "Weight")

        for (set in sets) {
            showSet(exerciseElement, set.id.toString(), set.reps.toString(), set.weight.toString())
        }

        container.addView(exerciseWrapper)
        return true
    }

    private suspend fun showSet(exerciseElement: TableLayout, id: String, reps: String, weight: String) {
        val setElement : TableRow = layoutInflater.inflate(R.layout.set_template, null) as TableRow

        val idView = setElement.findViewById<TextView>(R.id.id_number)
        idView.text = id

        val repsView = setElement.findViewById<TextView>(R.id.reps_number)
        repsView.text = reps

        val weightView = setElement.findViewById<TextView>(R.id.weight_number)
        weightView.text = weight

        exerciseElement.addView(setElement)
    }

    fun startTimer() {
        val notificationHelper = NotificationHelper(this)

        // Check if the timer is already running
        if (countdownTimer != null) {
            // Timer is already running, do nothing or handle as needed
            return
        }

        // Create a notification with an initial message
        val notificationId = System.currentTimeMillis().toInt()

        // Create a countdown timer with 60 seconds duration and 1-second intervals
        countdownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // This method will be called every second during the countdown
                val secondsRemaining = millisUntilFinished / 1000

                // Update the existing notification with the new message
                notificationHelper.sendUpdatableNotification(
                    "Riprendi ad allenarti!",
                    "Il timer sta per scadere. $secondsRemaining secondi rimasti.",
                    notificationId
                )
            }

            override fun onFinish() {
                // This method will be called when the countdown is finished
                // Handle any actions you want to perform when the timer finishes

                // Reset the timer
                countdownTimer = null
                notificationHelper.cancelNotification(notificationId)

                // Show a success toast when the timer finishes
                Toasty.success(this@WorkoutActivity, "Riprendi ad allenarti", Toast.LENGTH_SHORT, true).show()
            }
        }

        // Start the countdown timer
        countdownTimer?.start()
    }


}