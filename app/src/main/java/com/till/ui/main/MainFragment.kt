package com.till.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.work.*
import com.till.R
import com.till.adapters.ConnectionAdapter
import com.till.adapters.ConnectionListener
import com.till.databinding.MainFragmentBinding
import com.till.util.InjectorUtils
import com.till.workers.PushNotificationWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit


class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels {
        InjectorUtils.provideMainViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MainFragmentBinding.inflate(inflater)

        val adapter = ConnectionAdapter(object : ConnectionListener {
            override fun favoriteConnection() {
                Toast.makeText(context, "Favorited!", Toast.LENGTH_SHORT).show()
            }

            override fun navigateToConnectionFragment() {
                Toast.makeText(context, "NAVIGATE TO CONNECTION!", Toast.LENGTH_SHORT).show()
            }
        })

        binding.settingsButton.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToSettingsFragment())
        }

        binding.connectionSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    adapter.filter(it)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    adapter.filter(it)
                }
                return true
            }
        })

        binding.connectionList.adapter = adapter


        subscribeUi(adapter)

        context?.let { it ->
            // Check WorkManager if current job already is running
            WorkManager.getInstance(it)
                .getWorkInfosByTagLiveData(getString(R.string.scheduled_push_tag))
                .observe(this, Observer { list ->
                    if (list.isNullOrEmpty()) {
                        // If we can't find a job already running we create one
                        val request = PeriodicWorkRequestBuilder<PushNotificationWorker>(
                            15,
                            TimeUnit.MINUTES
                        )
                            .addTag("scheduled-push")
                            .setConstraints(
                                Constraints.Builder()
                                    .setRequiresDeviceIdle(false)
                                    .setRequiresCharging(false)
                                    .setRequiresBatteryNotLow(true)
                                    .build()
                            )
                            .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS
                            )
                            .build()

                        WorkManager.getInstance(it).enqueue(request)
                    } else {
                        list.stream().forEach { worker ->
                            Timber.i("Push worker ${worker.id} state: ${worker.state}")
                        }
                    }

                })
        }
        return binding.root
    }

    fun subscribeUi(adapter: ConnectionAdapter) {
        viewModel.connections.observe(this, Observer {
            adapter.submitList(it.toMutableList())
        })
    }
}
