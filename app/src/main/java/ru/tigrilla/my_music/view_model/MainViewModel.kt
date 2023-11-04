package ru.tigrilla.my_music.view_model

import android.content.res.AssetFileDescriptor
import android.media.MediaMetadataRetriever
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tigrilla.my_music.repository.MusicRepository
import ru.tigrilla.my_music.repository.entity.Track
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.coroutineContext


@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {
    enum class AddMusicStatus {
        SUCCESS,
        ALREADY_EXIST,
        OTHER_ERROR,
    }

    enum class DeleteMusicStatus {
        SUCCESS,
        OTHER_ERROR,
    }

    val tracks = musicRepository.allTracksOrderByLastUse().asLiveData()

    suspend fun addMusic(
        musicDir: File,
        filename: String,
        fileDescriptor: AssetFileDescriptor
    ) = viewModelScope.async(coroutineContext, CoroutineStart.UNDISPATCHED) {
        runCatching {
            addMusicImpl(musicDir, filename, fileDescriptor)
        }.getOrElse {
            AddMusicStatus.OTHER_ERROR
        }
    }

    private suspend fun addMusicImpl(
        musicDir: File,
        filename: String,
        fileDescriptor: AssetFileDescriptor
    ): AddMusicStatus {
        val metadata = MediaMetadataRetriever()

        metadata.setDataSource(fileDescriptor.fileDescriptor)

        val name = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).let {
            if (it.isNullOrEmpty()) {
                filename.removeSuffix(".mp3")
            } else {
                it
            }
        }
        val author =
            metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).orEmpty()
        val year = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            ?.toIntOrNull()
        val duration =
            metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()?.let {
                    Duration.ofSeconds(it / 1000)
                }
        val genre = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
        val bitrate = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            ?.toIntOrNull()
        val fileSize = fileDescriptor.parcelFileDescriptor.statSize

        if (musicRepository.checkTrackExist(name, author)) {
            return AddMusicStatus.ALREADY_EXIST
        }

        val newFilename = UUID.randomUUID().toString()

        File(musicDir, newFilename).outputStream().use { output ->
            fileDescriptor.createInputStream().use { input ->
                input.copyTo(output)
            }
        }

        musicRepository.insertTrack(
            Track(
                name,
                author,
                year,
                duration,
                genre,
                bitrate,
                newFilename,
                fileSize,
                LocalDateTime.now(),
            )
        )

        return AddMusicStatus.SUCCESS
    }

    fun deleteMusic(musicDir: File, track: Track) = viewModelScope.async {
        runCatching {
            val db = async(Dispatchers.IO) { musicRepository.deleteTrack(track) }
            val file = async(Dispatchers.IO) { File(musicDir, track.fileName).delete() }

            db.await()

            return@runCatching if (file.await()) DeleteMusicStatus.SUCCESS else DeleteMusicStatus.OTHER_ERROR
        }.getOrElse {
            DeleteMusicStatus.OTHER_ERROR
        }
    }
}
