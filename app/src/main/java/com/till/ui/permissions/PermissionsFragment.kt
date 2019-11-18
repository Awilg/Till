package com.till.ui.permissions

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.till.databinding.MainFragmentBinding
import com.till.ui.main.RequestCodes
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
		val binding = MainFragmentBinding.inflate(inflater)
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
				navigateToMain()
			}
		}
	}

	private fun navigateToMain() {
		findNavController().navigate(PermissionsFragmentDirections.actionPermissionsFragmentToMainFragment())
	}
}
