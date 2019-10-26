package com.till.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.till.data.Connection
import com.till.data.ConnectionRepository
import timber.log.Timber

class MainViewModel internal constructor(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    private val _connections = MutableLiveData<List<Connection>>()
    val connections: LiveData<List<Connection>>
        get() = _connections


    private val connectionList = MediatorLiveData<List<Connection>>()

    init {
        Timber.i("MainViewModel created...")
    }
}
