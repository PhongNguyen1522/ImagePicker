package com.phongnn.imagepicker.ui.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.phongnn.imagepicker.MainActivity
import com.phongnn.imagepicker.R
import com.phongnn.imagepicker.data.model.Song
import com.phongnn.imagepicker.data.utils.CommonConstant
import com.phongnn.imagepicker.ui.notification.NotificationChannel.Companion.CHANNEL_ID

class MusicService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent!!.getIntExtra("action", -1)
        try {
            val bundle = intent.extras
            if (bundle != null) {
                val song = bundle.get("object_song") as Song
                MainActivity.isPlaying = true
                startMusic(song)
                sendNotification(song)
            }
        } catch (e: java.lang.NullPointerException) {
            e.message
        }


        handleActionMusic(action)
        return START_NOT_STICKY
    }


    @SuppressLint("RemoteViewLayout")
    private fun sendNotification(song: Song) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("service_is_running", true)
            putExtra("name", song.title)
            putExtra("artist", song.artist)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("${song.title} is playing")
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
    }

    private fun handleActionMusic(action: Int) {
        when (action) {
            CommonConstant.PLAY -> {
                if (!MainActivity.isPlaying) {
                    MainActivity.isPlaying = true
                    mediaPlayer.start()
                }
            }
            CommonConstant.PAUSE -> {
                if (MainActivity.isPlaying) {
                    MainActivity.isPlaying = false
                    mediaPlayer.pause()
                }
            }
        }
    }

    private fun startMusic(song: Song) {
        mediaPlayer = MediaPlayer.create(applicationContext, song.path)
        if (!isPlaying) {
            isPlaying = true
            mediaPlayer.start()
        }
    }


    override fun stopService(name: Intent?): Boolean {
        isPlaying = false
        mediaPlayer.release()
        return super.stopService(name)
    }

    override fun onDestroy() {
        isPlaying = false
        super.onDestroy()
        mediaPlayer.stop()
    }

}