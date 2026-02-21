package com.prathamesh.womensafetyapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HelplineActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helpline)


        val callBtn = findViewById<Button>(R.id.btnCallHelpline)


        callBtn.setOnClickListener {
            val helplineNumber = "1091"
            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$helplineNumber"))


            if (callIntent.resolveActivity(packageManager) != null) {
                startActivity(callIntent)
            } else {
                Toast.makeText(this, "No dialer app found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
