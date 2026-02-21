package com.prathamesh.womensafetyapp.worker

import android.telephony.SmsManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prathamesh.womensafetyapp.data.AppDatabase
import com.prathamesh.womensafetyapp.data.SosMessage

class SmsWorker(appContext: android.content.Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val TAG = "SmsWorker"

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val pendingMessages = db.sosDao().getAllMessages()
        var allSent = true

        for (msg in pendingMessages) {
            try {
                SmsManager.getDefault().sendTextMessage(msg.phone, null, msg.message, null, null)
                db.sosDao().deleteMessage(msg)
                Log.d(TAG, "SMS sent successfully to ${msg.phone}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send SMS to ${msg.phone}: ${e.message}")
                allSent = false
            }
        }

        // Retry automatically if any message failed
        return if (allSent) Result.success() else Result.retry()
    }
}
