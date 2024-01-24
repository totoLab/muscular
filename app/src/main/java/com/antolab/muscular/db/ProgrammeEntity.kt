package com.antolab.muscular.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "programme",
    indices = [Index(value = ["name_it"], unique = true)]
)
data class ProgrammeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name_en") val name_en: String,
    @ColumnInfo(name = "name_es") val name_es: String,
    @ColumnInfo(name = "name_fr") val name_fr: String,
    @ColumnInfo(name = "name_de") val name_de: String,
    @ColumnInfo(name = "name_it") val name_it: String
    // Add other columns as needed
)
