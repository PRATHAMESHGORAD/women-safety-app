package com.prathamesh.womensafetyapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sos_messages")
data class SosMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phone: String,
    val message: String,
    val timestamp: Long
)
