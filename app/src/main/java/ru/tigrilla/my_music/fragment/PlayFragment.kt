package ru.tigrilla.my_music.fragment

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.tigrilla.my_music.R
import ru.tigrilla.my_music.databinding.MusicPlayFragmentBinding
import ru.tigrilla.my_music.view_model.PlayViewModel
import java.io.File
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toJavaDuration

@AndroidEntryPoint
class PlayFragment : Fragment() {
    private val args: PlayFragmentArgs by navArgs()

    private val viewModel: PlayViewModel by viewModels()

    private lateinit var player: MediaPlayer
    private lateinit var playerWatcher: Job
    private var watchPlayer = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = MediaPlayer()
        playerWatcher = watchMediaPlayer()

        player.apply {
            setOnCompletionListener { viewModel.next() }
            setOnPreparedListener {
                viewModel.duration.postValue(player.duration.milliseconds.toJavaDuration())
                start()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bindings = MusicPlayFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = this@PlayFragment.viewModel

            imageButtonPlayStopMusic.setOnClickListener { this@PlayFragment.viewModel.playResume() }
            imageButtonNextMusic.setOnClickListener { this@PlayFragment.viewModel.next() }
            imageButtonPrevMusic.setOnClickListener { this@PlayFragment.viewModel.prev() }

            lifecycleOwner = this@PlayFragment.viewLifecycleOwner

            imageButtonTrash.setOnClickListener { deleteCurrentTrack() }
            buttonDetails.setOnClickListener { openTrackDetails() }
            buttonMix.setOnClickListener { this@PlayFragment.viewModel.shuffleTracks() }

            musicSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    onMusicProgressChanged(progress, fromUser)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    onStartTrackingMusicProgress()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    onStopTrackingMusicProgress()
                }
            })
        }

        requireContext().let {
            viewModel.apply {
                pauseAction.observe(viewLifecycleOwner) {
                    player.pause()
                }

                resumeAction.observe(viewLifecycleOwner) {
                    player.start()
                }

                var skipFirstTime = viewModel.selectedPos.isInitialized
                playAction.observe(viewLifecycleOwner) {
                    if (skipFirstTime) {
                        skipFirstTime = false

                        return@observe
                    }

                    playNewFile(it)
                }

                repeatEnabled.observe(viewLifecycleOwner) { player.isLooping = it }

                viewModel.enabled.observe(viewLifecycleOwner, this@PlayFragment::onEnableChanged)
            }
        }

        if (viewModel.selectedPos.isInitialized) {
            return bindings.root
        }

        viewModel.apply {
            tracks = args.tracks.toMutableList()

            playFirst(args.selectedInx)
        }

        return bindings.root
    }

    private fun onEnableChanged(isEnabled: Boolean) {
        if (!isEnabled) {
            player.apply {
                seekTo(0)
                pause()
            }
        }
    }

    private fun deleteCurrentTrack() = lifecycleScope.launch(Dispatchers.IO) {
        runCatching {
            listOf(
                async { File(requireContext().filesDir, viewModel.currentTrack.fileName).delete() },
                async { viewModel.deleteCurrentTrack() }
            ).awaitAll()

            return@runCatching R.string.music_deleted_successfully
        }.getOrDefault(R.string.music_deleted_other_error).let { res ->
            requireActivity().let { activity ->
                activity.runOnUiThread { Toast.makeText(activity, res, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun onMusicProgressChanged(progress: Int, fromUser: Boolean) {
        if (!fromUser) {
            return
        }

        player.seekTo(progress.seconds.toInt(DurationUnit.MILLISECONDS))
    }

    private fun onStartTrackingMusicProgress() {
        watchPlayer = false
    }

    private fun onStopTrackingMusicProgress() {
        watchPlayer = true
    }

    private fun openTrackDetails() = findNavController().navigate(
        PlayFragmentDirections.actionPlayFragmentToInfoFragment(
            this@PlayFragment.viewModel.currentTrack
        )
    )

    private fun playNewFile(fileName: String) = lifecycleScope.launch(Dispatchers.IO) {
        player.apply {
            reset()
            setDataSource(File(requireContext().filesDir, fileName).path)
            prepareAsync()
        }
    }

    private fun watchMediaPlayer() = lifecycleScope.launch {
        while (true) {
            delay(100.milliseconds)

            if (watchPlayer) {
                viewModel.currentDuration.postValue(player.currentPosition.milliseconds.toJavaDuration())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        playerWatcher.cancel()

        player.release()
    }
}
