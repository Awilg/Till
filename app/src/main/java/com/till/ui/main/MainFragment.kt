package com.till.ui.main

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.work.*
import com.till.adapters.ConnectionAdapter
import com.till.adapters.ConnectionListener
import com.till.databinding.MainFragmentBinding
import com.till.util.InjectorUtils
import com.till.workers.PushNotificationWorker
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import timber.log.Timber
import java.util.concurrent.TimeUnit

enum class RequestCodes(val code: Int) {
    PERMISSIONS_RC_SMS_CONTACT(100)
}

class MainFragment : Fragment(), EasyPermissions.PermissionCallbacks {

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

        binding.connectionList.adapter = adapter

        // Check permissions for SMS
        if (EasyPermissions.hasPermissions(
                context!!,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.CALL_PHONE
            )
        ) {
            subscribeUi(adapter)
            context?.let { it ->
                WorkManager.getInstance(it)
                    .getWorkInfosByTagLiveData("scheduled-push")
                    .observe(this, Observer { list ->
                        if (list.isNullOrEmpty()) {
                            // If we can find a job already running we create one
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

                            WorkManager.getInstance(it).enqueue(request)
                        } else {
                            list.stream().forEach { worker ->
                                Timber.i("Push worker ${worker.id} state: ${worker.state}")
                            }
                        }
                    })
            }
        } else {
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(
                    this, RequestCodes.PERMISSIONS_RC_SMS_CONTACT.code,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.CALL_PHONE
                ).build()
            )
        }

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        when (requestCode) {
            RequestCodes.PERMISSIONS_RC_SMS_CONTACT.code -> {

            }
        }
    }

    private fun subscribeUi(adapter: ConnectionAdapter) {
        viewModel.connections.observe(this, Observer {
            adapter.submitList(it)
        })
    }
}
