package com.till.ui.permissions

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.till.databinding.PermissionsFragmentBinding
import com.till.ui.main.RequestCodes
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

class PermissionsFragment : Fragment(), EasyPermissions.PermissionCallbacks {

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		// Check permissions for SMS
		if (EasyPermissions.hasPermissions(
				context!!,
				Manifest.permission.READ_SMS,
				Manifest.permission.READ_CONTACTS,
				Manifest.permission.READ_CALL_LOG,
				Manifest.permission.CALL_PHONE
			)
		) {
			navigateToMain()
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
		val binding = PermissionsFragmentBinding.inflate(inflater)
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

		// (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
		// This will display a dialog directing them to enable the permission in app settings.
		if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
			AppSettingsDialog.Builder(this).build().show()
		}
	}

	override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
		when (requestCode) {
			RequestCodes.PERMISSIONS_RC_SMS_CONTACT.code -> {
				navigateToMain()
			}
			AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE -> {
				navigateToMain()
			}
		}
	}

	private fun navigateToMain() {
		findNavController().navigate(PermissionsFragmentDirections.actionPermissionsFragmentToMainFragment())
	}
}
