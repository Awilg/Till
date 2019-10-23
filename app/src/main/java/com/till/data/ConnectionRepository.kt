package com.till.data

class ConnectionRepository private constructor(private val connectionDao: ConnectionDao) {

	fun getConnections() = connectionDao.getConnections()

	fun getConnection(connectionId: Int) = connectionDao.getConnection(connectionId)

	fun addConnection(connection: Connection) = connectionDao.insert(connection)

	companion object {
		// For Singleton instantiation
		@Volatile private var instance: ConnectionRepository? = null

		fun getInstance(connectionDao: ConnectionDao) =
			instance ?: synchronized(this) {
				instance ?: ConnectionRepository(connectionDao).also { instance = it }
			}
	}
}
