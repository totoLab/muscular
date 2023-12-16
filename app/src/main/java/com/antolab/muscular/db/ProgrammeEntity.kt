package com.antolab.muscular.db

import androidx.room.*

@Entity(tableName = "programme")
data class ProgrammeEntity(
    @PrimaryKey
    val name: String
)
