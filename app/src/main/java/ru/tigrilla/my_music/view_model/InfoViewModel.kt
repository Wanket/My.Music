package ru.tigrilla.my_music.view_model

import androidx.lifecycle.ViewModel
import ru.tigrilla.my_music.repository.entity.Track

class InfoViewModel : ViewModel() {
    lateinit var track: Track

    fun formatDuration() = track.duration?.format()

    fun formatFileSize() = track.fileSize.let {
        if (it < 1024) {
            return@let "$it Bytes"
        }

        val kBytes = it / 1024
        if (kBytes < 1024) {
            return@let "$kBytes KB"
        } else {
            return@let "${kBytes / 1024} MB"
        }
    }

    fun formatBitrate() = track.bitrate?.let {
        if (it < 1024) {
            "$it bit/s"
        } else {
            "${it / 1024} kbit/s"
        }
    }
}