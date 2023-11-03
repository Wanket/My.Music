package ru.tigrilla.my_music.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PlayList(
    @PrimaryKey val name: String
)