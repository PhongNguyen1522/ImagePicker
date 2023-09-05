package com.phongnn.imagepicker.presenter.callback

import com.phongnn.imagepicker.data.model.Song

interface SongPassedListener {
    fun onSongPassed(song: Song, position: Int)

}