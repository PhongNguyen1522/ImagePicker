package com.phongnn.imagepicker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.phongnn.imagepicker.data.model.Song
import com.phongnn.imagepicker.databinding.ItemSongBinding
import com.phongnn.imagepicker.presenter.callback.SongPassedListener

class SongListAdapter(
    private val songList: List<Song>,
    private var songPassedListener: SongPassedListener?,
) : RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songList[position]
        holder.bind(song)
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.tvSongNameItem.text = song.title
            binding.tvSongWriterItem.text = song.artist

            binding.llSongItem.setOnClickListener {
                songPassedListener?.onSongPassed(song)
            }
        }
    }
}