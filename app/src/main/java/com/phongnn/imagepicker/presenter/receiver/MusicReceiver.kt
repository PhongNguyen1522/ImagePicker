package com.phongnn.imagepicker.presenter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.phongnn.imagepicker.data.utils.CommonConstant

class MusicReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val action = intent?.action
        if (action == CommonConstant.ACTION_PLAY) {
            updateButtonStateInActivity(context!!, false)
        } else if (action == CommonConstant.ACTION_PAUSE) {
            updateButtonStateInActivity(context!!, true)
        }
    }

    private fun updateButtonStateInActivity(context: Context, state: Boolean) {
        val updateIntent = Intent(CommonConstant.ACTION_UPDATE_BUTTON_STATE)
        updateIntent.putExtra(
            "button_state", state
        )
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent)
    }


}