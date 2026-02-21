package com.prathamesh.womensafetyapp.data

import androidx.room.*

@Dao
interface SosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: SosMessage)

    @Delete
    suspend fun deleteMessage(message: SosMessage)

    @Query("SELECT * FROM sos_messages")
    suspend fun getAllMessages(): List<SosMessage>
}
