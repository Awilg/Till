package com.till.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.till.workers.SeedDatabaseWorker

@Database(entities = [Connection::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
	abstract fun connectionDao(): ConnectionDao

	companion object {
		// For Singleton instantiation
		@Volatile
		private var instance: AppDatabase? = null

		fun getInstance(context: Context): AppDatabase {
			return instance ?: synchronized(this) {
				instance ?: buildDatabase(context).also {
					instance = it

					prepopulateDB(context)
				}
			}
		}

		private fun prepopulateDB(context: Context) {
			val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>().build()
			WorkManager.getInstance(context).enqueue(request)
		}

		private fun buildDatabase(context: Context): AppDatabase {
			return Room.databaseBuilder(context, AppDatabase::class.java, "TillDB").build()
		}
	}
}
