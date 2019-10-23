package com.till.util

import android.content.Context
import com.till.data.AppDatabase
import com.till.data.ConnectionRepository
import com.till.ui.main.MainViewModelFactory

object InjectorUtils {

	private fun getConnectionRepository(context: Context): ConnectionRepository {
		val db = AppDatabase.getInstance(context.applicationContext)
		val dao = 	db.connectionDao()
		return ConnectionRepository.getInstance(dao)
	}

	fun provideMainViewModelFactory(
		context: Context
	): MainViewModelFactory {
		val repository = getConnectionRepository(context)
		return MainViewModelFactory(repository)
	}
}
