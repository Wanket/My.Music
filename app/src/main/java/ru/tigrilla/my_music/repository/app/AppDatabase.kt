package ru.tigrilla.my_music.repository.app

import ru.tigrilla.my_music.repository.converter.LocalDateTimeConverter
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.tigrilla.my_music.repository.MusicRepository
import ru.tigrilla.my_music.repository.converter.DurationConverter
import ru.tigrilla.my_music.repository.entity.Track

@Database(entities = [Track::class], version = 1)
@TypeConverters(DurationConverter::class, LocalDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicRepository(): MusicRepository
}