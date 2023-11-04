package ru.tigrilla.my_music.fragment

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.tigrilla.my_music.R
import ru.tigrilla.my_music.adapter.TrackListAdapter
import ru.tigrilla.my_music.databinding.MainFragmentBinding
import ru.tigrilla.my_music.repository.entity.Track
import ru.tigrilla.my_music.view_model.MainViewModel

@AndroidEntryPoint
class MainFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()

    private val trackAdapter = TrackListAdapter()

    private val addMusicResult =
        registerForActivityResult(ActivityResultContracts.OpenDocument(), this::onMusicFileSelected)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bindings = MainFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = this@MainFragment.viewModel

            buttonAddMusic.setOnClickListener { addMusic() }

            recyclerView.apply {
                adapter = trackAdapter
                layoutManager = LinearLayoutManager(this@MainFragment.context)
            }
        }

        viewModel.tracks.observeForever { trackAdapter.submitList(it) }

        trackAdapter.deleteTrack.observeForever { deleteMusic(it) }

        return bindings.root
    }

    private fun deleteMusic(track: Track) = lifecycleScope.launch {
        viewModel.deleteMusic(requireContext().filesDir, track).await().let {
            requireActivity().runOnUiThread { onMusicDeleted(it) }
        }
    }

    private fun onMusicDeleted(status: MainViewModel.DeleteMusicStatus) {
        when (status) {
            MainViewModel.DeleteMusicStatus.SUCCESS -> R.string.music_deleted_successfully
            MainViewModel.DeleteMusicStatus.OTHER_ERROR -> R.string.music_deleted_other_error
        }.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onMusicAdded(status: MainViewModel.AddMusicStatus) {
        when (status) {
            MainViewModel.AddMusicStatus.SUCCESS -> R.string.music_added_successfully
            MainViewModel.AddMusicStatus.ALREADY_EXIST -> R.string.music_already_exist
            MainViewModel.AddMusicStatus.OTHER_ERROR -> R.string.music_added_other_error
        }.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMusic() {
        addMusicResult.launch(arrayOf("audio/mpeg"))
    }

    private fun onMusicFileSelected(uri: Uri?) {
        if (uri == null) {
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            requireContext().contentResolver.apply {
                query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()

                    return@use cursor.getString(nameIndex)
                }?.let { filename ->
                    openAssetFileDescriptor(uri, "r")?.let { file ->
                        file.use {
                            val status =
                                viewModel.addMusic(requireContext().filesDir, filename, file)
                                    .await()

                            requireActivity().runOnUiThread { onMusicAdded(status) }
                        }
                    }
                }
            }
        }
    }
}
