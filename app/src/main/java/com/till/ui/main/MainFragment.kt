package com.till.ui.main

import android.content.Context
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
import com.till.adapters.ConnectionAdapter
import com.till.adapters.ConnectionListener
import com.till.databinding.MainFragmentBinding
import com.till.util.InjectorUtils
import com.till.workers.PushNotificationWorker
import com.till.workers.SeedDatabaseWorker
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
            // Add an observer to trigger the push worker once the seed worker is finished
            WorkManager.getInstance(it)
                .getWorkInfosByTagLiveData("seed-db")
                .observe(this, Observer { list ->
                    if (list.isNullOrEmpty()) {
                        // Initial DB seed
                        updateDb(it)
                    }
                    if (list.stream().anyMatch { work -> work.state.isFinished }) {
                        // Once completed we can start the push worker
                        initPushWorker(it)
                    }
                })

        }
        return binding.root
    }

    private fun updateDb(context: Context) {
        val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>()
            .addTag("seed-db").build()

        WorkManager.getInstance(context).enqueue(request)
    }

    private fun initPushWorker(context: Context) {
        val request = PeriodicWorkRequestBuilder<PushNotificationWorker>(
            1,
            TimeUnit.DAYS
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

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "periodic-push-worker",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun subscribeUi(adapter: ConnectionAdapter) {
        viewModel.connections.observe(this, Observer {
            adapter.submitList(it.toMutableList())
        })
    }
}
