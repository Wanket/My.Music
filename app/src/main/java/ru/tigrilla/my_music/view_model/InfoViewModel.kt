package ru.tigrilla.my_music.view_model

import androidx.lifecycle.ViewModel
import ru.tigrilla.my_music.repository.entity.Track

class InfoViewModel : ViewModel() {
    lateinit var track: Track

    fun formatDuration() = track.duration?.let {
        val h = it.seconds / 3600
        val m = it.seconds % 3600 / 60
        val s = it.seconds % 60

        if (h > 0) {
            "%02d:%02d:%02d".format(h, m, s)
        } else {
            "%02d:%02d".format(m, s)
        }
    }

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