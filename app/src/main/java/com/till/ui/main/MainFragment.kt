package com.till.ui.main

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.till.R
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import timber.log.Timber

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Check permissions for SMS
        if (EasyPermissions.hasPermissions(context!!, Manifest.permission.READ_SMS)) {
            getSmsMessages()
        } else {
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(
                    this, 101,
                    Manifest.permission.READ_SMS
                ).build()
            )
        }

        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

    fun getSmsMessages() {
        // public static final String INBOX = "content://sms/inbox";
        // public static final String SENT = "content://sms/sent";
        // public static final String DRAFT = "content://sms/draft";
        val cursor = (activity as Activity).contentResolver.query(
            Uri.parse("content://sms/inbox"),
            null,
            null,
            null,
            null
        )

        var numbers: Set<String> = emptySet()

        if (cursor != null) {
            if (cursor.moveToFirst()) { // must check the result to prevent exception
                do {
                    numbers =
                        numbers.plus(cursor.getString(cursor.getColumnIndexOrThrow("address")))
                    var msgData = ""
                    for (i in 0 until cursor.columnCount) {
                        msgData += " " + cursor.getColumnName(i) + ":" + cursor.getString(i)
                    }
                    Timber.i(msgData)
                    // use msgData
                } while (cursor.moveToNext())
            }

            cursor.close()
        }

        Timber.i(numbers.toString())
    }
}
