package com.phongnn.imagepicker.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phongnn.imagepicker.MainActivity
import com.phongnn.imagepicker.R
import com.phongnn.imagepicker.data.model.Song
import com.phongnn.imagepicker.databinding.FragmentSongListDialogBinding
import com.phongnn.imagepicker.presenter.callback.MusicServiceListener
import com.phongnn.imagepicker.presenter.callback.SongPassedListener
import com.phongnn.imagepicker.ui.adapter.SongListAdapter
import com.phongnn.imagepicker.ui.service.MusicService

class SongListDialogFragment(
    private val songList: List<Song>,
    private var musicServiceListener: MusicServiceListener?,
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentSongListDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSongListDialogBinding.inflate(LayoutInflater.from(requireContext()))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rcvSongList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SongListAdapter(songList, object : SongPassedListener {
                override fun onSongPassed(song: Song, position: Int) {

                    MainActivity.currentSongPosition = position

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
        setMaxPeekHeight()
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    private fun setMaxPeekHeight() {
        val constraintLayout = view as ConstraintLayout
        val layoutParams = constraintLayout.layoutParams as ViewGroup.LayoutParams

        // Adjust the maximum peek height by modifying the view's layout parameters
        val maximumPeekHeight = resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)
        layoutParams.height = maximumPeekHeight
        constraintLayout.layoutParams = layoutParams
    }

}