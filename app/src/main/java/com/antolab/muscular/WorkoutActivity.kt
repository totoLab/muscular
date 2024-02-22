package com.antolab.muscular

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import com.antolab.muscular.db.AppDao
import com.antolab.muscular.db.ExerciseEntity
import com.antolab.muscular.db.SetEntity
import com.antolab.muscular.utils.NotificationHelper
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.abs

class WorkoutActivity : AppCompatActivity() {

    private lateinit var appDao: AppDao
    private lateinit var currentProgramme: String
    private lateinit var button: Button
    private lateinit var selectedLanguage: String

    private lateinit var preferences: SharedPreferences
    private val PREF_BUTTON_STATE = "pref_button_state"
    private val PREF_CHOSEN_TIMER = "pref_chosen_timer"
    private var lastSensorEventTime: Long = 0

    private var working = false
    private var firstNotificationSent = false
    private var chosenTimer = 0

    private var countdownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_workout)

    initializeViews()

    // Check if the timer value is already stored in preferences
    if (!preferences.contains(PREF_CHOSEN_TIMER)) {
        // If not, set it to the default value of 60
        preferences.edit {
            putInt(PREF_CHOSEN_TIMER, 60)
        }
    }

    initializeTimer()

    if (savedInstanceState != null) {
        restoreInstanceState(savedInstanceState)
    } else {
        restorePreferences()
    }

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

    setButtonClickListeners()
}
    private fun initializeViews() {
        button = findViewById(R.id.working)
        preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        selectedLanguage = loadLocate()
    }

    private fun initializeTimer() {
        val numberPicker: NumberPicker = findViewById(R.id.numberPicker)
        numberPicker.minValue = 60
        numberPicker.maxValue = 300

        chosenTimer = preferences.getInt(PREF_CHOSEN_TIMER, 60)
        numberPicker.value = chosenTimer

        numberPicker.setOnValueChangedListener { _, _, newVal ->
            chosenTimer = newVal
        }
    }

    private fun restorePreferences() {
        working = preferences.getBoolean(PREF_BUTTON_STATE, false)
        updateButtonState()
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        initializeTimer()
        working = savedInstanceState.getBoolean(PREF_BUTTON_STATE, false)
        chosenTimer = savedInstanceState.getInt(PREF_CHOSEN_TIMER, 60)
        updateButtonState()
    }

    private fun setButtonClickListeners() {
        button.setOnClickListener {
            toggleButtonState()
        }
    }

    private fun toggleButtonState() {
        if (working) {
            working = false
            button.setBackgroundColor(resources.getColor(android.R.color.holo_green_light, theme))
            button.text = getString(R.string.inizia_allenamento)
        } else {
            working = true
            button.setBackgroundColor(resources.getColor(android.R.color.holo_red_light, theme))
            button.text = getString(R.string.ferma_allenamento)
        }
    }

    private fun updateButtonState(backgroundColorResId: Int = android.R.color.holo_green_light, buttonTextResId: Int = R.string.inizia_allenamento) {
        button.setBackgroundColor(resources.getColor(backgroundColorResId, theme))
        button.text = getString(buttonTextResId)
    }

    private fun loadLocate(): String {
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("My_Lang", "it") ?: "it"
    }

    override fun onStart() {
        super.onStart()
        setupExerciseViews()
        setupAccelerometerListener()
    }

    private fun setupExerciseViews() {
        val container = findViewById<LinearLayout>(R.id.workout_exercises_container) ?: return

        MainScope().launch {
            val exerciseCount = appDao.getExerciseOfProgrammeCount(currentProgramme)
            if (exerciseCount >= 1) {
                for (exercise in appDao.getAllExerciseOfProgramme(currentProgramme)) {
                    val addingOutcome: Boolean = showExercise(container, exercise)
                    Log.d("exerciseProgrammeLoading", "$exercise was ${if (addingOutcome) "" else "not"} added to the scroll view")
                }
            }
        }
    }

    private fun setupAccelerometerListener() {
        val accelerometer = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    handleAccelerometerEvent(event)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                // Not needed
            }
        }

        accelerometer.registerListener(
            accelerometerListener,
            accelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private fun handleAccelerometerEvent(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()

        if (!firstNotificationSent && currentTime - lastSensorEventTime > 2 * 60 * 1000 && abs(event.values[0]) > 10) {
            lastSensorEventTime = currentTime
            startTimer()
            firstNotificationSent = true
        }
    }

    private suspend fun showExercise(container: LinearLayout, exercise: ExerciseEntity): Boolean {
        val exerciseWrapper: ConstraintLayout =
            layoutInflater.inflate(R.layout.exercise_instance_wrapper, null) as ConstraintLayout

        val title: TextView = exerciseWrapper.findViewById(R.id.exercise_name)
        title.text = getLocalizedExerciseName(exercise)

        val exerciseElement: TableLayout = exerciseWrapper.findViewById(R.id.exercise_instance)
        val sets: List<SetEntity> = appDao.getAllSetsOfExercise(exercise.id)

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
        val setElement: TableRow = layoutInflater.inflate(R.layout.set_template, null) as TableRow

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
            else -> exercise.name_it
        }
    }

    private fun startTimer() {
        if (working) {
            val notificationHelper = NotificationHelper(this)

            // Check if the timer is already running
            if (countdownTimer != null) {
                return
            }

            val notificationId = System.currentTimeMillis().toInt()

            countdownTimer = object : CountDownTimer(chosenTimer.toLong() * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000

                    // Update the existing notification with the new message
                    notificationHelper.sendUpdatableNotification(
                        getString(R.string.Riprendi),
                        getString(R.string.Timer) + " $secondsRemaining " + getString(R.string.Rimasto),
                        notificationId
                    )
                }

                override fun onFinish() {
                    countdownTimer = null
                    notificationHelper.cancelNotification(notificationId)
                    firstNotificationSent = false

                    Toasty.success(
                        this@WorkoutActivity,
                        getString(R.string.Riprendi),
                        Toast.LENGTH_SHORT,
                        true
                    ).show()
                }
            }

            countdownTimer?.start()
        }
    }

    override fun onStop() {
        super.onStop()
        // save the button state and timer value when the activity is interrupted
        preferences.edit {
            putBoolean(PREF_BUTTON_STATE, working)
            putInt(PREF_CHOSEN_TIMER, chosenTimer)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(PREF_BUTTON_STATE, working)
        outState.putInt(PREF_CHOSEN_TIMER, chosenTimer)
    }


}