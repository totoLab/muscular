package com.antolab.muscular.utils

import androidx.room.TypeConverter
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

class Converters {
    @TypeConverter
    fun durationToLong(duration: Duration): Long = duration.inWholeMilliseconds / 1000

    @TypeConverter
    fun longToDuration(value: Long): Duration = value.seconds

}