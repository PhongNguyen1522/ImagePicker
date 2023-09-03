package com.phongnn.imagepicker.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.phongnn.imagepicker.MainActivity
import com.phongnn.imagepicker.data.model.Song
import com.phongnn.imagepicker.databinding.FragmentSongListDialogBinding
import com.phongnn.imagepicker.presenter.callback.MusicServiceListener
import com.phongnn.imagepicker.presenter.callback.SongPassedListener
import com.phongnn.imagepicker.ui.adapter.SongListAdapter
import com.phongnn.imagepicker.ui.service.MusicService

class SongListDialogFragment(
    private val songList: List<Song>,
    private var musicServiceListener: MusicServiceListener?,
) : DialogFragment() {

    private lateinit var binding: FragmentSongListDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentSongListDialogBinding.inflate(LayoutInflater.from(requireContext()))
        binding.rcvSongList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SongListAdapter(songList, object : SongPassedListener {
                override fun onSongPassed(song: Song) {

                    val intent = Intent(requireActivity(), MusicService::class.java)
                    // Start to play music
                    val bundle = Bundle()
                    bundle.putParcelable("object_song", song)
                    intent.putExtras(bundle)
                    intent.action = MusicService.ACTION_PLAY
                    requireContext().startService(intent)
                    // Update Info in MainActivity
                    musicServiceListener?.onMusicIsPlaying(song)

                }
            })
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

}