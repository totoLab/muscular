package com.antolab.muscular.adapter

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.antolab.muscular.R
import com.antolab.muscular.db.AppDao
import com.antolab.muscular.db.ExerciseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExercisesAdapter(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val appDao: AppDao,
    private var dataSet: List<ExerciseEntity>
) : RecyclerView.Adapter<ExercisesAdapter.ViewHolder>() {


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView : ImageView
        val nameView : TextView
        val descriptionView : TextView
        val deleteButton : Button

        init {
            // Define click listener for the ViewHolder's View
            imageView = view.findViewById(R.id.image_thumb_button)
            nameView = view.findViewById(R.id.exerciseName)
            descriptionView = view.findViewById(R.id.exerciseDescription)
            deleteButton = view.findViewById<Button>(R.id.exercise_delete_button)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.exercise_template, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val exercise = dataSet[position]

        val imageHolder = viewHolder.imageView
        val id: Int = imageId(exercise.image)
        if (id == 0) {
            imageHolder.visibility = View.GONE
        } else {
            imageHolder.setImageResource(id)
            imageHolder.setOnClickListener {
                showFullscreenImage(null, id)
            }
        }

        viewHolder.nameView.text = exercise.name
        viewHolder.descriptionView.text = exercise.description

        viewHolder.deleteButton.setOnClickListener {
            handleDeleteClick(exercise)
        }
    }

    private fun handleDeleteClick(exercise: ExerciseEntity) {
        dataSet = dataSet.filter { it != exercise }
        notifyDataSetChanged()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                appDao.deleteExercise(exercise)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun updateData(newData: List<ExerciseEntity>) {
        dataSet = newData
        notifyDataSetChanged()
    }

    fun showFullscreenImage(view: View?, imageId: Int) {
        // Create a dialog with a custom layout
        val fullscreenDialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        fullscreenDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        fullscreenDialog.setContentView(R.layout.dialog_fullscreen_image)

        val fullscreenImageView = fullscreenDialog.findViewById<ImageView>(R.id.fullscreenImageView)
        fullscreenImageView.setImageResource(imageId)
        fullscreenDialog.show()
    }

    private fun imageId(imagePath: String): Int {
        return context.resources.getIdentifier(imagePath, "drawable", context.packageName)
    }


}
