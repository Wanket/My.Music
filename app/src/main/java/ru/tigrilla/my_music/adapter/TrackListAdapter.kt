package ru.tigrilla.my_music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.tigrilla.my_music.databinding.TrackViewItemBinding
import ru.tigrilla.my_music.repository.entity.Track

class TrackListAdapter: ListAdapter<Track, TrackListAdapter.TrackViewHolder>(TrackComparator()) {
    private val deleteTrackMutable = MutableLiveData<Track>()

    val deleteTrack: LiveData<Track> = deleteTrackMutable

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val current = getItem(position)

        holder.binding.apply {
            textViewName.text = current.name
            textViewAuthor.text = current.author

            imageButtonDelete.setOnClickListener { deleteTrackMutable.value = current }
        }
    }

    class TrackViewHolder(val binding: TrackViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun create(parent: ViewGroup) = TrackViewHolder(
                TrackViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    private class TrackComparator : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track) =
            oldItem.fileName == newItem.fileName

        override fun areContentsTheSame(oldItem: Track, newItem: Track) = oldItem == newItem
    }
}