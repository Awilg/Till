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
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.till.adapters.ConnectionAdapter
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

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels {
        InjectorUtils.provideMainViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = MainFragmentBinding.inflate(inflater)

        val adapter = ConnectionAdapter()
        binding.connectionList.adapter = adapter
        subscribeUi(adapter)

        // Check permissions for SMS
        if (EasyPermissions.hasPermissions(
                context!!,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.CALL_PHONE
            )
        ) {
            Timber.i("We gud on permssss")
            context?.let {
                val request =
                    PeriodicWorkRequestBuilder<PushNotificationWorker>(
                        1,
                        TimeUnit.DAYS
                    ).build()
                WorkManager.getInstance(it).enqueue(request)
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
                Toast.makeText(context, "Toastin'", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun subscribeUi(adapter: ConnectionAdapter) {
        viewModel.connections.observe(this, Observer {
            adapter.submitList(it)
        })
    }
}
