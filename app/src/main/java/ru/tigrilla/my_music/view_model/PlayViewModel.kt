package ru.tigrilla.my_music.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.tigrilla.my_music.repository.MusicRepository
import ru.tigrilla.my_music.repository.entity.Track
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

@HiltViewModel
class PlayViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {
    lateinit var tracks: MutableList<Track>

    private val selectedPosMutable = MutableLiveData<Int>()

    val selectedPos: LiveData<Int> = selectedPosMutable

    private val playActionMutable = MutableLiveData<String>()

    val playAction: LiveData<String> = playActionMutable

    private val pauseActionMutable = MutableLiveData<Unit>()

    val pauseAction: LiveData<Unit> = pauseActionMutable

    private val resumeActionMutable = MutableLiveData<Unit>()

    val resumeAction: LiveData<Unit> = resumeActionMutable

    private val isPlayingMutable = MutableLiveData(false)

    val isPlaying: LiveData<Boolean> = isPlayingMutable

    val repeatEnabled = MutableLiveData(false)

    val currentTrack: Track
        get() = tracks[selectedPos.value!!]

    val duration = MutableLiveData(Duration.ZERO)

    val currentDuration = MutableLiveData(Duration.ZERO)

    private val enabledMutable = MutableLiveData(true)

    val enabled: LiveData<Boolean> = enabledMutable

    fun formatDuration(duration: Duration) = duration.format()

    fun playFirst(selectedPos: Int) {
        selectedPosMutable.value = selectedPos

        playActionMutable.value = currentTrack.fileName

        isPlayingMutable.value = true

        currentDuration.value = Duration.ZERO

        updateLastUse(currentTrack)
    }

    fun playResume() {
        isPlayingMutable.value = !isPlaying.value!!

        if (isPlaying.value!!) {
            resumeActionMutable.value = Unit
        } else {
            pauseActionMutable.value = Unit
        }

        updateLastUse(currentTrack)
    }

    fun next() {
        selectedPosMutable.value = (selectedPos.value!! + 1) % tracks.size

        playActionMutable.value = currentTrack.fileName

        currentDuration.value = Duration.ZERO

        updateLastUse(currentTrack)
    }

    fun prev() {
        selectedPosMutable.value = (selectedPos.value!! + tracks.size - 1) % tracks.size

        playActionMutable.value = currentTrack.fileName

        currentDuration.value = Duration.ZERO

        updateLastUse(currentTrack)
    }

    fun deleteCurrentTrack() = tracks.removeAt(selectedPos.value!!).also {
        if (tracks.isNotEmpty()) {
            val newPos = selectedPos.value!! % tracks.size

            selectedPosMutable.postValue(newPos)

            playActionMutable.postValue(tracks[newPos].fileName)

            currentDuration.postValue(Duration.ZERO)

            updateLastUse(tracks[newPos])
        } else {
            enabledMutable.postValue(false)

            duration.postValue(0.milliseconds.toJavaDuration())
        }

        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.deleteTrack(it)
        }
    }

    fun shuffleTracks() {
        tracks.shuffle()

        selectedPosMutable.value = 0

        playActionMutable.value = currentTrack.fileName

        currentDuration.value = Duration.ZERO
    }

    private fun updateLastUse(track: Track) = track.let {
        it.lastUse = LocalDateTime.now()

        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.updateTracks(listOf(it))
        }
    }
}