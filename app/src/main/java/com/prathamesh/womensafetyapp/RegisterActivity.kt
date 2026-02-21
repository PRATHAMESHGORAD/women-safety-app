package com.prathamesh.womensafetyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.prathamesh.womensafetyapp.data.AppDatabase
import com.prathamesh.womensafetyapp.data.User


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val nameEditText = findViewById<EditText>(R.id.editTextName)
        val phoneEditText = findViewById<EditText>(R.id.editTextPhone)
        val registerButton = findViewById<Button>(R.id.buttonRegister)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            var phone = phoneEditText.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // ✅ Always prepend +91 if missing
                if (!phone.startsWith("+91")) {
                    phone = "+91$phone"
                }

                // ✅ Save to local Room Database
                val db = AppDatabase.getDatabase(this)
                val userDao = db.userDao()

                GlobalScope.launch {
                    userDao.insert(User(name = name, phone = phone))
                }

                Toast.makeText(this, "✅ Data Saved! Name: $name, Phone: $phone", Toast.LENGTH_SHORT).show()
            }
        }

        val viewRegisteredNumbersButton = findViewById<Button>(R.id.viewRegisteredNumbersButton)
        viewRegisteredNumbersButton.setOnClickListener {
            val intent = Intent(this, RegisteredNumbersActivity::class.java)
            startActivity(intent)
        }
    }
}
