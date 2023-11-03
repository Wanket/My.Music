package ru.tigrilla.my_music.repository.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset


object LocalDateTimeConverter {
    @TypeConverter
    fun fromUnixTime(unixTime: Long?) = unixTime?.let {
        LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
    }


    @TypeConverter
    fun toUnixTime(date: LocalDateTime?) = date?.toEpochSecond(ZoneOffset.UTC)
}