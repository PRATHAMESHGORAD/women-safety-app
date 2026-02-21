package com.prathamesh.womensafetyapp

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfilePageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    companion object {
        private const val REQUEST_CAMERA = 100
        private const val REQUEST_GALLERY = 101
        private const val PERMISSION_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailText = findViewById<TextView>(R.id.textViewEmail)
        val usernameText = findViewById<TextView>(R.id.textViewUsername)
        val logoutButton = findViewById<Button>(R.id.buttonLogout)
        val profileButton = findViewById<ImageButton>(R.id.buttonProfilePhoto)

        // Display email
        val currentUser = auth.currentUser
        emailText.text = "Email: ${currentUser?.email ?: "Not available"}"


        val userId = currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    var username = document.getString("username")
                    if (username.isNullOrEmpty()) {
                        username = currentUser.displayName
                    }
                    if (!username.isNullOrEmpty()) {
                        usernameText.text = "Username: $username"
                    } else {
                        usernameText.text = "Username: Not set"
                        promptToSetUsername(userId, usernameText, currentUser)
                    }
                }
                .addOnFailureListener {
                    usernameText.text = "Error loading username"
                }
        } else {
            usernameText.text = "Username: Not available"
        }

        // Logout button
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Profile photo click
        profileButton.setOnClickListener {
            checkPermissionsAndShowOptions()
        }
    }

    private fun promptToSetUsername(
        userId: String,
        usernameText: TextView,
        currentUser: com.google.firebase.auth.FirebaseUser?
    ) {
        val input = EditText(this)
        input.hint = "Enter username"

        val dialog = AlertDialog.Builder(this)
            .setTitle("Set username")
            .setMessage("Please enter a username to complete your profile.")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newUsername = input.text.toString().trim()
                if (newUsername.isNotEmpty()) {
                    val userMap = hashMapOf("username" to newUsername)
                    db.collection("users").document(userId)
                        .set(userMap, SetOptions.merge())
                        .addOnSuccessListener {
                            currentUser?.let { u ->
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(newUsername)
                                    .build()
                                u.updateProfile(profileUpdates)
                            }
                            usernameText.text = "Username: $newUsername"
                        }
                        .addOnFailureListener {
                            usernameText.text = "Error saving username"
                        }
                }
            }
            .setNegativeButton("Skip") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun checkPermissionsAndShowOptions() {
        val permissionsNeeded = mutableListOf<String>()

        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(android.Manifest.permission.CAMERA)
        }
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissions(permissionsNeeded.toTypedArray(), PERMISSION_CODE)
        } else {
            showPhotoOptions()
        }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Photo")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val profileButton = findViewById<ImageButton>(R.id.buttonProfilePhoto)
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    profileButton.setImageBitmap(bitmap)
                }
                REQUEST_GALLERY -> {
                    val uri = data?.data
                    profileButton.setImageURI(uri)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showPhotoOptions()
            } else {
                Toast.makeText(this, "Camera and Storage permissions are required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
