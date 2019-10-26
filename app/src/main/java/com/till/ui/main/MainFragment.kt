package com.till.ui.main

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.till.MainActivity
import com.till.R
import com.till.adapters.ConnectionAdapter
import com.till.databinding.MainFragmentBinding
import com.till.notif.NotificationHelper
import com.till.util.InjectorUtils
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

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
            Toast.makeText(context, "Toastin'", Toast.LENGTH_SHORT).show()
            //viewModel.getConnections()

            context?.let {
                // Create an explicit intent for an Activity in your app
                val intent = Intent(it, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent: PendingIntent = PendingIntent.getActivity(it, 0, intent, 0)

                NotificationHelper.createNotificationChannel(
                    it,
                    4, // HIGH
                    false,
                    "Test Channel",
                    "This is a test channel description!"
                )

                val builder = NotificationHelper.createSampleDataNotification(
                    it,
                    "This is a title",
                    "This is a message POG",
                    "This is BIG TEXT",
                    pendingIntent = pendingIntent,
                    autoCancel = true
                )

                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data =
                        Uri.parse("smsto:" + getString(R.string.default_phone_num))  // This ensures only SMS apps respond
                    putExtra("sms_body", getString(R.string.sms_template_omg))
                }

                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                    data =
                        Uri.parse("tel:" + getString(R.string.ryan_phone_num))  // This ensures only SMS apps respond
                    putExtra("sms_body", getString(R.string.sms_template_omg))
                }

                // Verify it resolves
                val activities: List<ResolveInfo> =
                    (activity as Activity).packageManager.queryIntentActivities(smsIntent, 0)
                val isIntentSafe: Boolean = activities.isNotEmpty()

                val smsPendingIntent: PendingIntent =
                    PendingIntent.getActivity(
                        context,
                        0,
                        smsIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )

                val callPendingIntent: PendingIntent =
                    PendingIntent.getActivity(
                        context,
                        0,
                        callIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )

                if (isIntentSafe) {
                    builder.addAction(
                        R.drawable.ic_launcher_foreground, getString(R.string.text),
                        smsPendingIntent
                    )
                }
                builder.addAction(
                    R.drawable.ic_launcher_foreground, getString(R.string.call),
                    callPendingIntent
                )

                NotificationHelper.showNotification(it, builder)
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
