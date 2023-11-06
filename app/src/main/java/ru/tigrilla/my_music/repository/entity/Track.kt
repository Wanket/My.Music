package ru.tigrilla.my_music.repository.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import kotlinx.parcelize.Parcelize
import java.time.Duration
import java.time.LocalDateTime

@Parcelize
@Entity(primaryKeys = ["name", "author"], indices = [Index(value = ["fileName"], unique = true)])
data class Track(
    val name: String,
    val author: String,
    val year: Int?,
    val duration: Duration?,
    val genre: String?,
    val bitrate: Int?,
    val fileName: String,
    val fileSize: Long,
    var lastUse: LocalDateTime?
) : Parcelable
