package ru.tigrilla.my_music.repository.entity

import androidx.room.Entity
import androidx.room.Index
import java.time.Duration
import java.time.LocalDateTime

@Entity(primaryKeys = ["name", "author"], indices = [Index(value = ["fileName"], unique = true)])
data class Track(
    val name: String,
    val author: String,
    val year: Int?,
    val duration: Duration?,
    val genre: String?,
    val bitrate: Int?,
    val fileName: String,
    val lastUse: LocalDateTime?
)