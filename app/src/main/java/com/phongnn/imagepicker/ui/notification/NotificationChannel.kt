package com.phongnn.imagepicker.ui.notification

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class  NotificationChannel : Application() {

    companion object {
        const val CHANNEL_ID = "CHANNEL_SERVICE_EXAMPLE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    // From the 8th android, we have to create Channel Notification ID
    @SuppressLint("ObsoleteSdkInt")
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID,"MyNotification", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.setSound(null, null)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}