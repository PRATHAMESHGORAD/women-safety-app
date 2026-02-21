package com.prathamesh.womensafetyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.prathamesh.womensafetyapp.data.AppDatabase
import com.prathamesh.womensafetyapp.data.User


class RegisteredNumbersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registered_numbers)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = AppDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {
            val users = db.userDao().getAllUsers().toMutableList()

            // ✅ Always add police number first
            users.add(0, User(id = -1, name = "Police", phone = "100"))

            withContext(Dispatchers.Main) {
                recyclerView.adapter = UserAdapter(
                    this@RegisteredNumbersActivity,
                    users,
                    db.userDao()   // ✅ pass dao also
                )
            }
        }
    }
}
