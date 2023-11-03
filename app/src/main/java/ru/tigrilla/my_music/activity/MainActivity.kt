package ru.tigrilla.my_music.activity

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tigrilla.my_music.R
import ru.tigrilla.my_music.adapter.TrackListAdapter
import ru.tigrilla.my_music.databinding.MainActivityBinding
import ru.tigrilla.my_music.repository.MusicRepository
import ru.tigrilla.my_music.repository.entity.Track
import ru.tigrilla.my_music.view_model.MainViewModel
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val trackAdapter = TrackListAdapter()

    private val addMusicResult =
        registerForActivityResult(ActivityResultContracts.OpenDocument(), this::onMusicFileSelected)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity).apply {
            viewModel = this@MainActivity.viewModel

            buttonAddMusic.setOnClickListener { addMusic() }

            recyclerView.adapter = trackAdapter
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        }

        viewModel.tracks.observeForever { trackAdapter.submitList(it) }

        trackAdapter.deleteTrack.observeForever { deleteMusic(it) }
    }

    private fun deleteMusic(track: Track) = lifecycleScope.launch {
        viewModel.deleteMusic(filesDir, track).await().let {
            runOnUiThread { onMusicDeleted(it) }
        }
    }


    private fun onMusicDeleted(status: MainViewModel.DeleteMusicStatus) {
        when (status) {
            MainViewModel.DeleteMusicStatus.SUCCESS -> R.string.music_deleted_successfully
            MainViewModel.DeleteMusicStatus.OTHER_ERROR -> R.string.music_deleted_other_error
        }.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onMusicAdded(status: MainViewModel.AddMusicStatus) {
        when (status) {
            MainViewModel.AddMusicStatus.SUCCESS -> R.string.music_added_successfully
            MainViewModel.AddMusicStatus.ALREADY_EXIST -> R.string.music_already_exist
            MainViewModel.AddMusicStatus.OTHER_ERROR -> R.string.music_added_other_error
        }.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
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
            contentResolver.apply {
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
                                viewModel.addMusic(this@MainActivity.filesDir, filename, file)
                                    .await()

                            runOnUiThread { onMusicAdded(status) }
                        }
                    }
                }
            }
        }
    }
}
