package ru.tigrilla.my_music.repository.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import java.time.LocalDateTime

@Entity(
    primaryKeys = ["trackName", "trackAuthor", "playListName"],
    foreignKeys = [
        ForeignKey(
            entity = PlayList::class,
            parentColumns = ["name"],
            childColumns = ["playListName"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Track::class,
            parentColumns = ["name", "author"],
            childColumns = ["trackName", "trackAuthor"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class TrackPlayList(
    val trackName: String,
    val trackAuthor: String,
    val playListName: String,
    val lastUse: LocalDateTime
)