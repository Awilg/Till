package com.till.ui.main

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.till.R
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

    var numbers: Set<String> = emptySet()
    var names: Set<String> = emptySet()
    var calls: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Check permissions for SMS
        if (EasyPermissions.hasPermissions(
                context!!,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG
            )
        ) {
            Toast.makeText(context, "Toastin'", Toast.LENGTH_SHORT).show()
            viewModel.getConnections()
        } else {
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(
                    this, RequestCodes.PERMISSIONS_RC_SMS_CONTACT.code,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG
                ).build()
            )
        }

        return inflater.inflate(R.layout.main_fragment, container, false)
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
                viewModel.getConnections()
            }
        }
    }
}
