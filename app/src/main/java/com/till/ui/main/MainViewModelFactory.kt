package com.till.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.till.data.ConnectionRepository

class MainViewModelFactory(
	private val repository: ConnectionRepository
) : ViewModelProvider.NewInstanceFactory() {

	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel> create(modelClass: Class<T>) = MainViewModel(repository) as T
}
