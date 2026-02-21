package com.prathamesh.womensafetyapp.data

import androidx.room.*

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User)

    @Query("UPDATE users SET name = :name, phone = :phone WHERE id = :id")
    suspend fun updateNamePhone(id: Int, name: String, phone: String)

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int)
}
