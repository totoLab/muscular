package com.antolab.muscular

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antolab.muscular.db.AppDao
import com.antolab.muscular.utils.PrePopulation
import com.antolab.muscular.adapter.ExercisesAdapter
import kotlinx.coroutines.launch

class ExercisesActivity : AppCompatActivity() {

    private lateinit var appDao: AppDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExercisesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        val database = MyApplication.appDatabase
        appDao = database.appDao()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExercisesAdapter(this, lifecycleScope, appDao, emptyList())
        recyclerView.adapter = adapter

        val buttonAdd: Button = findViewById(R.id.button_exercise_new)
        buttonAdd.setOnClickListener {
            lifecycleScope.launch {
                handleAddButtonClick()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            handleStart()
        }
    }

    private suspend fun handleStart() {
        val exercises = appDao.getAllExercises()

        if (exercises.isEmpty()) {
            val empty = findViewById<TextView>(R.id.exercices_default_empty)
            empty.visibility = View.VISIBLE
        } else {
            adapter.updateData(exercises)
        }
    }

    private suspend fun handleAddButtonClick() {
        if (appDao.getExercisesCount() == 0) {
            val instance = PrePopulation(this@ExercisesActivity)
            instance.exercisesPrepopulation()
            instance.setPrepopulation()
        } else {
            Toast.makeText(this@ExercisesActivity, "DB is not empty", Toast.LENGTH_LONG).show()
        }
    }
}
