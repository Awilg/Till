package com.till.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.till.data.Connection
import com.till.data.ConnectionRepository
import timber.log.Timber

class MainViewModel internal constructor(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    val connections: LiveData<List<Connection>>
        get() = connectionRepository.getConnections()

    init {
        Timber.i("MainViewModel created...")
    }
}
