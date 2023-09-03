package com.phongnn.imagepicker.ui.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.phongnn.imagepicker.MainActivity
import com.phongnn.imagepicker.R
import com.phongnn.imagepicker.data.model.Song
import com.phongnn.imagepicker.data.utils.CommonConstant
import com.phongnn.imagepicker.presenter.receiver.MusicReceiver
import com.phongnn.imagepicker.ui.notification.NotificationChannel.Companion.CHANNEL_ID
import java.io.IOException

class MusicService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private var currentSong: Song? = null
    private var musicAction = -1

    companion object {
        const val ACTION_PLAY = "com.phongnn.action.PLAY"
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        mediaSession = MediaSessionCompat(baseContext, "My Music")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(CommonConstant.MY_LOG_TAG, "onStartCommand is called")
        musicAction = intent!!.getIntExtra("action", -1)

        when (intent.action) {
            ACTION_PLAY -> {
                try {
                    val bundle = intent.extras
                    if (bundle != null) {
                        val song = bundle.get("object_song") as Song
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
            MainActivity.isPlaying = false
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
        mediaPlayer.reset()
        try {
            if (!MainActivity.isPlaying) {
                mediaPlayer.setDataSource(this, song.contentUri)
                mediaPlayer.prepare()
                mediaPlayer.start()
                currentSong = song
                MainActivity.isPlaying = true
                sendNotification(song)
//                createNotification(song)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("RemoteViewLayout", "LaunchActivityFromNotification", "UnspecifiedImmutableFlag")
    private fun sendNotification(song: Song) {

        // Define an action string for the notification click
        val notificationClickAction = "com.example.app.NOTIFICATION_CLICK"

        // Set up pending intent for notification click to bring the app to the foreground
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mainActivityIntent.action = notificationClickAction
        mainActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.music_desc_img)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0).setMediaSession(mediaSession.sessionToken)
            )
            .setContentText(song.artist)
            .setContentTitle(song.title).setLargeIcon(bitmap)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                R.drawable.ic_pause, "Pause", getPendingIntent(this, CommonConstant.ACTION_PAUSE)
            )
            .addAction(
                R.drawable.ic_play, "Play", getPendingIntent(this, CommonConstant.ACTION_PLAY)
            )
            .build()
        startForeground(1, notification)

    }

    @SuppressLint("RemoteViewLayout", "LaunchActivityFromNotification", "UnspecifiedImmutableFlag")
    private fun createNotification(song: Song) {

        // Define an action string for the notification click
        val notificationClickAction = "com.example.app.NOTIFICATION_CLICK"

        val customLayout = RemoteViews(packageName, R.layout.layout_custom_notification).also {
            it.setTextViewText(R.id.tv_song_name_notification, song.title)
            it.setTextViewText(R.id.tv_song_writer_notification, song.artist)
            if (!mediaPlayer.isPlaying) {
                it.setImageViewResource(R.id.ic_play_pause_notification, R.drawable.ic_play)
                it.setOnClickPendingIntent(
                    R.id.ic_play_pause_notification,
                    getPendingIntent(this, CommonConstant.ACTION_PLAY)
                )
            } else {
                it.setImageViewResource(R.id.ic_play_pause_notification, R.drawable.ic_pause)
                it.setOnClickPendingIntent(
                    R.id.ic_play_pause_notification,
                    getPendingIntent(this, CommonConstant.ACTION_PAUSE)
                )
            }
        }

        // Set up pending intent for notification click to bring the app to the foreground
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mainActivityIntent.action = notificationClickAction
        mainActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_music_note)
            .setCustomContentView(customLayout)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        startForeground(1, notification)

    }

    private fun getPendingIntent(context: Context, action: String): PendingIntent? {
        val playOrPauseIntent = Intent(this, MusicReceiver::class.java)
        playOrPauseIntent.action = action
        return PendingIntent.getBroadcast(
            context,
            0,
            playOrPauseIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

}