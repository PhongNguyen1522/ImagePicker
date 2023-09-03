package com.phongnn.imagepicker.ui.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Notification.MediaStyle
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.phongnn.imagepicker.MainActivity
import com.phongnn.imagepicker.R
import com.phongnn.imagepicker.data.model.Song
import com.phongnn.imagepicker.data.utils.CommonConstant
import com.phongnn.imagepicker.databinding.LayoutCustomNotificationBinding
import com.phongnn.imagepicker.ui.notification.NotificationChannel.Companion.CHANNEL_ID
import java.io.IOException

class MusicService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = false
    private var currentSong: Song? = null

    companion object {
        const val ACTION_PLAY = "com.phongnn.action.PLAY"
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // get action for play or pause music
        val musicAction = intent!!.getIntExtra("action", -1)
//        try {
//            val bundle = intent.extras
//            if (bundle != null) {
//                val song = bundle.get("object_song") as Song
//                MainActivity.isPlaying = true
//                startMusic(song)
//                sendNotification(song)
//            }
//        } catch (e: java.lang.NullPointerException) {
//            e.message
//        }
        when (intent.action) {
            ACTION_PLAY -> {
                try {
                    val bundle = intent.extras
                    if (bundle != null) {
                        val song = bundle.get("object_song") as Song
                        if (song != currentSong)
                            MainActivity.isPlaying = true
                        stopCurrentSong()
                        startMusic(song)
                    }
                } catch (e: java.lang.NullPointerException) {
                    e.message
                }
            }
        }
        handleActionMusic(musicAction)
        return START_NOT_STICKY
    }

    private fun stopCurrentSong() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
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
//        mediaPlayer.reset()
//        mediaPlayer = MediaPlayer.create(applicationContext, song.contentUri)
//        if (!isPlaying) {
//            isPlaying = true
//            mediaPlayer.start()
//        }
//        currentSong = song
//        sendNotification(song)
        mediaPlayer.reset()
        try {
            mediaPlayer.setDataSource(this, song.contentUri)
            mediaPlayer.prepare()
            mediaPlayer.start()
            currentSong = song
            sendNotification(song)
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle the exception (e.g., show an error message)
        }
    }

    @SuppressLint("RemoteViewLayout", "LaunchActivityFromNotification", "UnspecifiedImmutableFlag")
    private fun sendNotification(song: Song) {

        // Define an action string for the notification click
        val notificationClickAction = "com.example.app.NOTIFICATION_CLICK"

        val notificationLayout = RemoteViews(packageName, R.layout.layout_custom_notification)

//        val mainIntent = Intent(this, MainActivity::class.java).apply {
//            putExtra("service_is_running", true)
//            putExtra("name", song.title)
//            putExtra("artist", song.artist)
//        }
        val playPendingIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MusicService::class.java).apply {
                action = CommonConstant.PLAY.toString()
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        notificationLayout.setOnClickPendingIntent(R.id.ic_play_pause, playPendingIntent)

        val pausePendingIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MusicService::class.java).apply {
                action = CommonConstant.PAUSE.toString()
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        notificationLayout.setOnClickPendingIntent(R.id.ic_play_pause, pausePendingIntent)

        // Set up pending intent for notification click to bring the app to the foreground
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mainActivityIntent.action = notificationClickAction
        mainActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE)


        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_music_note)
            .setCustomContentView(notificationLayout)
            .setContentIntent(pendingIntent)
            .setContentTitle("Wonderful music")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isPlaying = false
        mediaPlayer.release()
    }

}