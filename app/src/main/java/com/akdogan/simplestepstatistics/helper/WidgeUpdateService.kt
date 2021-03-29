package com.akdogan.simplestepstatistics.helper

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.akdogan.simplestepstatistics.repository.GoogleFitCommunicator
import com.akdogan.simplestepstatistics.widget.callBackCreator

const val ACTION_UPDATE_WIDGET = "com.akdogan.simplestepstatistics.action.update_widget"
const val TAG_JOB = "JobIntentService"
const val JOB_ID = 1000


fun enqueueWidgetUpdate(context: Context){
    val intent = Intent(context, WidgetUpdateService::class.java).apply {
        action = ACTION_UPDATE_WIDGET
    }
    JobIntentService.enqueueWork(context, WidgetUpdateService::class.java, JOB_ID, intent)
}

class WidgetUpdateService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        if (intent.action == ACTION_UPDATE_WIDGET) {
            //val res = resources
            val callback = callBackCreator(this)
            GoogleFitCommunicator(this).accessGoogleFitStatic(callback)
        }
    }
}