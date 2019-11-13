package com.till.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connections ORDER BY name")
    fun getConnections(): LiveData<List<Connection>>

    @Query("SELECT * FROM connections WHERE number = :number")
    fun getConnection(number: String): LiveData<Connection>

    @Query("SELECT * FROM connections ORDER BY lastContact")
    fun getConnectionsByLastContact(): LiveData<List<Connection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(connections: List<Connection>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(connection: Connection)
}
