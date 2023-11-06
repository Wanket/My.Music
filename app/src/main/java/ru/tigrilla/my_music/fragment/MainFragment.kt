package ru.tigrilla.my_music.fragment

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import ru.tigrilla.my_music.repository.MusicRepository
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

            imageButtonMix.setOnClickListener { this@MainFragment.viewModel.mixMusic() }

            imageButtonSort.setOnClickListener(this@MainFragment::showSortMenu)
        }

        viewModel.tracks.observe(viewLifecycleOwner, trackAdapter::submitList)

        trackAdapter.deleteTrack.observe(viewLifecycleOwner, this::deleteMusic)

        return bindings.root
    }

    private fun showSortMenu(anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            inflate(R.menu.sort_menu)

            setOnMenuItemClickListener(this@MainFragment::sortMenuItemSelected)

            show()
        }
    }

    private val menuSortTypeToDBSortType = mapOf(
        R.id.menu_track_name to MainViewModel.SortType.TRACK_NAME,
        R.id.menu_author to MainViewModel.SortType.AUTHOR,
        R.id.menu_year to MainViewModel.SortType.RELEASE_YEAR,
        R.id.menu_genre to MainViewModel.SortType.GENRE,
        R.id.menu_last_use to MainViewModel.SortType.LAST_USE
    )

    private fun sortMenuItemSelected(item: MenuItem): Boolean {
        viewModel.sortMusic(menuSortTypeToDBSortType[item.itemId]!!)

        return false
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
