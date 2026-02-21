package com.prathamesh.womensafetyapp

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.IBinder
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.prathamesh.womensafetyapp.data.AppDatabase
import com.prathamesh.womensafetyapp.data.SosMessage



class SOSService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        triggerSOS()
        return START_NOT_STICKY
    }

    private fun triggerSOS() {
        sendSOSMessage()
        sendLocation()
        callPolice()
        Toast.makeText(this, "🚨 SOS Triggered!", Toast.LENGTH_SHORT).show()
        stopSelf()
    }

    private fun sendSOSMessage() {
        val message = "🚨 I am in danger! Please help me."
        val db = AppDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {
            val users = db.userDao().getAllUsers()
            for (user in users) {
                user.phone?.let { rawNumber ->
                    val number = if (rawNumber.startsWith("+91")) rawNumber else "+91$rawNumber"
                    try {
                        SmsManager.getDefault().sendTextMessage(number, null, message, null, null)
                    } catch (e: Exception) {
                        // Save to database for retry
                        db.sosDao().insert(
                            SosMessage(
                                phone = number,
                                message = message,
                                timestamp = System.currentTimeMillis()
                            )
                        )

                    }
                }
            }
        }
    }


    private fun sendLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val message = "📍 Location: https://maps.google.com/?q=${it.latitude},${it.longitude}"
                val db = AppDatabase.getDatabase(this)
                CoroutineScope(Dispatchers.IO).launch {
                    val users = db.userDao().getAllUsers()
                    for (user in users) {
                        user.phone?.let { rawNumber ->
                            val number = if (rawNumber.startsWith("+91")) rawNumber else "+91$rawNumber"
                            try {
                                SmsManager.getDefault().sendTextMessage(number, null, message, null, null)
                            } catch (e: Exception) {
                                db.sosDao().insert(
                                    SosMessage(
                                        phone = number,
                                        message = message,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )

                            }
                        }
                    }
                }
            }
        }
    }


    private fun callPolice() {
        val policeNumber = "100" // Or "112"
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$policeNumber"))
        callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent)
        }
    }
}
