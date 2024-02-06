package com.antolab.muscular.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Common fields
    val name: String,
    val description: String,
    val image: String,

    // Translations for different languages
    @ColumnInfo(name = "name_en")
    val name_en: String,

    @ColumnInfo(name = "name_es")
    val name_es: String,

    @ColumnInfo(name = "name_de")
    val name_de: String,

    @ColumnInfo(name = "name_fr")
    val name_fr: String,

    @ColumnInfo(name = "name_it")
    val name_it: String,

    @ColumnInfo(name = "description_en")
    val description_en: String,

    @ColumnInfo(name = "description_es")
    val description_es: String,

    @ColumnInfo(name = "description_de")
    val description_de: String,

    @ColumnInfo(name = "description_fr")
    val description_fr: String,

    @ColumnInfo(name = "description_it")
    val description_it: String,

    @ColumnInfo(name = "language")
    val language: String = "en"  // Default language, you might want to change this
)
