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

	@Query("SELECT * FROM connections WHERE id = :connectionId")
	fun getConnection(connectionId: Int): LiveData<Connection>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAll(connections: List<Connection>)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(connection: Connection)
}
