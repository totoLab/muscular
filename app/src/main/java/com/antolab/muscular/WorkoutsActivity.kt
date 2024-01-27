package com.antolab.muscular

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.antolab.muscular.utils.PrePopulation
import kotlinx.coroutines.*
import com.antolab.muscular.db.*

class WorkoutsActivity : AppCompatActivity() {
    private lateinit var appDao: AppDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts)


        val database = MyApplication.appDatabase
        appDao = database.appDao()

        val button_add : Button = findViewById(R.id.button_programme_new)
        button_add.setOnClickListener {
            GlobalScope.launch {
                if (appDao.getProgrammesCount() == 0) {
                    val instance = PrePopulation(this@WorkoutsActivity, appDao)
                    instance.programmesPrepopulation()
                    instance.pePrepopulation()
                } else {
                    Toast.makeText(this@WorkoutsActivity, "DB is not empty", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val container = findViewById<LinearLayout>(R.id.container) ?: return

        MainScope().launch {
            if (appDao.getProgrammesCount() == 0) {
                val empty = findViewById<TextView>(R.id.programmes_default_empty)
                empty.visibility = View.VISIBLE
                return@launch
            } else {
                // add exercises dynamically
                for (programme in appDao.getAllProgrammes()) {
                    val adding_outcome: Boolean = showProgramme(container, programme)
                    Log.d("programmeLoading", "$programme was ${if (adding_outcome) "" else "not"} added to the scroll view")
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val container = findViewById<LinearLayout>(R.id.container) ?: return
        container.removeAllViews()
    }



    private fun showProgramme(container: LinearLayout, programme: ProgrammeEntity): Boolean {
        // Inflate the exercise template and make it visible
        val programmeElement : ConstraintLayout = layoutInflater.inflate(R.layout.programme_template, null) as ConstraintLayout
        programmeElement.visibility = View.VISIBLE

        // Setup information about the exercise
        // name
        val textViewProgrammeName = programmeElement.findViewById<TextView>(R.id.programme_name)

        // Handle translation for program name
        val programName = when (loadLocate()) {
            "en" -> programme.name_en
            "es" -> getTranslatedProgrammeName(programme, "es")
            "fr" -> getTranslatedProgrammeName(programme, "fr")
            "it" -> getTranslatedProgrammeName(programme, "it")
            "de" -> getTranslatedProgrammeName(programme, "de")
            else -> programme.name_en // Fallback to default name
        }
        textViewProgrammeName.text = programName

        // deleting specific exercise from database
        val deleteButton = programmeElement.findViewById<Button>(R.id.programme_delete_button)
        deleteButton.setOnClickListener {
            container.removeView(programmeElement)
            MainScope().launch {
                appDao.deleteProgramme(programme)
                Log.d("exerciseDeletion", "Deleted $programme from the list.")
            }
        }

        textViewProgrammeName.setOnClickListener {
            val intent = Intent(this, WorkoutActivity::class.java)
            intent.putExtra("programmeName", programme.name_it)
            startActivity(intent)
        }

        container.addView(programmeElement)
        return true
    }

    private fun getTranslatedProgrammeName(programme: ProgrammeEntity, language: String): String {
        return when (language) {
            "es" -> programme.name_es
            "fr" -> programme.name_fr
            "it" -> programme.name_it
            "de" -> programme.name_de
            else -> programme.name_it // Fallback to default name (English)
        }
    }

    private fun loadLocate(): String {
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("My_Lang", "") ?: ""
    }

}