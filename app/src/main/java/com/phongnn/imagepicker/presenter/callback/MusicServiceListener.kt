package com.phongnn.imagepicker.presenter.callback

import com.phongnn.imagepicker.data.model.Song

interface MusicServiceListener {
    fun onMusicIsPlaying(song: Song)
}