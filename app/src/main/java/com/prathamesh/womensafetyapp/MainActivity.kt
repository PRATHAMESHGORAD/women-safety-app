package com.prathamesh.womensafetyapp

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.prathamesh.womensafetyapp.data.AppDatabase
import com.prathamesh.womensafetyapp.data.SosMessage
import com.prathamesh.womensafetyapp.data.User
import com.prathamesh.womensafetyapp.receiver.NetworkChangeReceiver
import com.prathamesh.womensafetyapp.worker.SmsWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSION_REQUEST_CODE = 100
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        FirebaseApp.initializeApp(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        val filter = android.content.IntentFilter().apply {
            addAction("android.net.conn.CONNECTIVITY_CHANGE")
            addAction(android.content.Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        registerReceiver(NetworkChangeReceiver(), filter)



        scheduleSmsWorker()



        insertTestUserIfEmpty()




        if (intent.getBooleanExtra("TRIGGER_SOS", false)) triggerSOS()

        // SOS button
        val sosButton = findViewById<ImageButton>(R.id.sosButton)
        sosButton.setOnLongClickListener {
            if (checkAndRequestPermissions()) triggerSOS()
            true
        }

        // Navigation cards
        findViewById<CardView>(R.id.cardRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        findViewById<CardView>(R.id.cardPolice).setOnClickListener {
            startActivity(Intent(this, PoliceStationActivity::class.java))
        }
        findViewById<CardView>(R.id.cardLocation).setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }
        findViewById<CardView>(R.id.cardProfile).setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            startActivity(Intent(this, if (user != null) ProfilePageActivity::class.java else LoginActivity::class.java))
        }
        findViewById<CardView>(R.id.cardHelpline).setOnClickListener {
            startActivity(Intent(this, HelplineActivity::class.java))
        }
        findViewById<CardView>(R.id.cardCrimeStats).setOnClickListener {
            startActivity(Intent(this, CrimeStatsActivity::class.java))
        }
    }

    // ==================== SOS ====================
    private fun triggerSOS() {
        sendSOSLocation()
        sendSOSAlert()
        makePoliceCall()
        Toast.makeText(this, "🚨 SOS Triggered!", Toast.LENGTH_SHORT).show()
    }

    private fun makePoliceCall() {
        val policeNumber = "100"
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$policeNumber"))
            startActivity(callIntent)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 2)
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissionsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            permissionsNeeded.add(Manifest.permission.SEND_SMS)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)

        return if (permissionsNeeded.isEmpty()) {
            true
        } else {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
            false
        }
    }

    private fun sendSOSLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val message = "📍 My location: https://maps.google.com/?q=${it.latitude},${it.longitude}"
                sendToAllUsers(message)
            }
        }
    }

    private fun sendSOSAlert() {
        val message = "🚨 I am in danger! Please help me."
        sendToAllUsers(message)
    }

    private fun sendToAllUsers(message: String) {
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val users = db.userDao().getAllUsers()
            withContext(Dispatchers.Main) {
                for (user in users) {
                    val number = if (user.phone.startsWith("+91")) user.phone else "+91${user.phone}"
                    sendMessageOrSave(number, message)
                }
            }
        }
    }

    private fun sendMessageOrSave(number: String, message: String) {
        val isAirplaneModeOn = Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0

        if (isAirplaneModeOn) {
            Log.d(TAG, "Airplane Mode ON, saving message to Room")
            savePendingMessageToDb(number, message)
            scheduleSmsWorker()
        } else {
            try {
                SmsManager.getDefault().sendTextMessage(number, null, message, null, null)
                Log.d(TAG, "SMS sent -> $number")
            } catch (e: Exception) {
                Log.e(TAG, "SMS failed: ${e.message}")
                savePendingMessageToDb(number, message)
                scheduleSmsWorker()
            }
        }
    }

    private fun savePendingMessageToDb(number: String, message: String) {
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            db.sosDao().insert(SosMessage(phone = number, message = message, timestamp = System.currentTimeMillis()))
            Log.d(TAG, "Saved pending SOS -> $number")
        }
    }

    private fun scheduleSmsWorker() {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()

        val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.prathamesh.womensafetyapp.worker.SmsWorker>()
            .setConstraints(constraints)
            .build()

        androidx.work.WorkManager.getInstance(this)
            .enqueueUniqueWork("send_pending_sms", androidx.work.ExistingWorkPolicy.KEEP, workRequest)

        android.util.Log.d("MainActivity", "SmsWorker scheduled with network constraint")
    }


    // ==================== Permissions ====================
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            triggerSOS()
        }
        if (requestCode == 2 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            makePoliceCall()
        }
    }

    // ==================== Debug Helpers ====================
    private fun insertTestUserIfEmpty() {
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val users = db.userDao().getAllUsers()
            if (users.isEmpty()) {
                // 🔧 Replace with your own number to test
                db.userDao().insert(User(name = "Test", phone = "+911234567890"))
                Log.d(TAG, "Test user inserted")
            }
        }
    }

    private fun dumpPendingMessagesToLog() {
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val pending = db.sosDao().getAllMessages()
            pending.forEach { Log.d(TAG, "Pending: ${it.phone} -> ${it.message}") }
        }
    }
}
