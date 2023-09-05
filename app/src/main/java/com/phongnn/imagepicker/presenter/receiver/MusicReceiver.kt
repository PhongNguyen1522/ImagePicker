package com.phongnn.imagepicker.presenter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.phongnn.imagepicker.data.utils.CommonConstant

class MusicReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            CommonConstant.ACTION_PLAY -> {
                updateButtonStateInActivity(context!!, CommonConstant.ACTION_PLAY)
            }
            CommonConstant.ACTION_PAUSE -> {
                updateButtonStateInActivity(context!!, CommonConstant.ACTION_PAUSE)
            }
            CommonConstant.ACTION_PREVIOUS_SONG -> {
                updateButtonStateInActivity(context!!, CommonConstant.ACTION_PREVIOUS_SONG)
            }
            CommonConstant.ACTION_NEXT_SONG -> {
                updateButtonStateInActivity(context!!, CommonConstant.ACTION_NEXT_SONG)
            }
        }
    }

    // Past data to mainActivity
    private fun updateButtonStateInActivity(context: Context, state: String) {
        val updateIntent = Intent(CommonConstant.ACTION_UPDATE_BUTTON_STATE)
        updateIntent.putExtra(
            "button_state", state
        )
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent)
    }


}