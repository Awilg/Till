package com.till.ui.main

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.PhoneLookup
import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.till.R
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import timber.log.Timber

enum class RequestCodes(val code: Int) {
    PERMISSIONS_RC_SMS_CONTACT(100)
}

class MainFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    var numbers: Set<String> = emptySet()
    var names: Set<String> = emptySet()

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Check permissions for SMS
        if (EasyPermissions.hasPermissions(
                context!!,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CONTACTS
            )
        ) {
            scrape()
        } else {
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(
                    this, RequestCodes.PERMISSIONS_RC_SMS_CONTACT.code,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_CONTACTS
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
                scrape()
            }
        }
    }

    private fun scrape() {
        Timber.i("Scraping started...")

        getSmsMessages()

        Timber.i("Scraped ${numbers.size} numbers from sms messages...")
        Timber.i("Matching names to numbers...")

        getContactsNames()

        Timber.i("Matched ${names.size} names...")
    }

    private fun getSmsMessages() {
        // public static final String INBOX = "content://sms/inbox";
        // public static final String SENT = "content://sms/sent";
        // public static final String DRAFT = "content://sms/draft";
        val cursor = (activity as Activity).contentResolver.query(
            Uri.parse("content://sms/inbox"),
            Array(1) { Telephony.Sms.ADDRESS },
            null,
            null,
            null
        )

        // Some providers return null if an error occurs, others throw an exception
        when (cursor?.count) {
            null -> {
                Timber.e("Error fetching sms messages from provider")
            }
            0 -> {
                Timber.i("Sms message query failed. Try again.")
            }
            else -> {
                cursor.apply {
                    val index: Int = getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                    /*
                     * Moves to the next row in the cursor. Before the first movement in the
                     * cursor, the "row pointer" is -1, and if you try to retrieve data at that
                     * position you will get an exception.
                     */
                    moveToFirst()

                    do {
                        numbers = numbers.plus(getString(index))
                    } while (moveToNext())
                }
                cursor.close()
            }
        }

        Timber.i(numbers.toString())
    }

    private fun getContactsNames() {
        numbers.forEach {
            val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(it))
            val cursor = (activity as Activity).contentResolver.query(
                uri, null, null, null, null
            )

            when (cursor?.count) {
                null -> {
                    Timber.e("Error fetching contact info for number $it")
                }
                0 -> {
                    Timber.i("Unable to find contact for number $it.")
                }
                else -> {
                    cursor.apply {
                        val index: Int = getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME)
                        moveToFirst()
                        names = names.plus(getString(index))
                    }
                }
            }
            cursor?.close()
        }
    }
}
