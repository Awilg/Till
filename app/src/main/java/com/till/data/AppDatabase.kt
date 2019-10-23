package com.till.data

import android.content.Context
import androidx.databinding.adapters.Converters
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.till.workers.SeedDatabaseWorker
import timber.log.Timber

@Database(entities = [Connection::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
	abstract fun connectionDao(): ConnectionDao

	companion object {
		// For Singleton instantiation
		@Volatile private var instance: AppDatabase? = null

		fun getInstance(context: Context): AppDatabase {
			Timber.i("AppDatabase GET INSTANCE!")
			return instance ?: synchronized(this) {
				instance ?: buildDatabase(context).also {
					Timber.i("DB BUILT!")
					instance = it
				}
			}
		}

		// Create and pre-populate the database.
		private fun buildDatabase(context: Context): AppDatabase {
			Timber.i("BUILDING DA DB!")
			return Room.databaseBuilder(context, AppDatabase::class.java, "TillDB")
				.addCallback(object : RoomDatabase.Callback() {
					override fun onCreate(db: SupportSQLiteDatabase) {
						Timber.i("ON CREATE CALLBACK!")
						super.onCreate(db)
						val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>().build()
						WorkManager.getInstance(context).enqueue(request)
					}
				})
				.build()
		}
	}
}
