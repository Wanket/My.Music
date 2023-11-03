package ru.tigrilla.my_music.repository.converter

import androidx.room.TypeConverter
import java.time.Duration

class DurationConverter {
    @TypeConverter
    fun fromInt(seconds: Long?) = seconds?.let {
        Duration.ofSeconds(it)
    }

    @TypeConverter
    fun toLong(duration: Duration?) = duration?.seconds
}