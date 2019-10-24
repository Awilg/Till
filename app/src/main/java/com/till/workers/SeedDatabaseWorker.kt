package com.till.workers

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.till.data.AppDatabase
import com.till.data.Connection
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class SeedDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private var smsNumbers: MutableSet<Contact> = mutableSetOf()
    private var callLogNumbers: MutableSet<Contact> = mutableSetOf()
    private var allNumbers: Set<Contact> = mutableSetOf()

    var conns: MutableList<Connection> = mutableListOf()

    class Contact(val number: String, val contactDate: String) {

        override fun toString(): String {
            return "Contact(number='$number', contactDate=$contactDate)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Contact

            if (number != other.number) return false
            if (contactDate != other.contactDate) return false

            return true
        }

        override fun hashCode(): Int {
            var result = number.hashCode()
            result = 31 * result + contactDate.hashCode()
            return result
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        try {
            scrape()

            Timber.i("Connections made - size ${conns.size}")

            val database = AppDatabase.getInstance(applicationContext)
            database.connectionDao().insertAll(conns)

            Result.success()
        } catch (ex: Exception) {
            Timber.e("Error seeding database: $ex")
            Result.failure()
        }
    }

    private suspend fun scrape() = coroutineScope {
        Timber.i("Scraping started...")

        coroutineScope {
            launch {
                Timber.i("Getting sms numbers...")
                getSmsMessages()
                Timber.i("Scraped ${smsNumbers.size} numbers from sms messages...")
            }

            launch {
                Timber.i("Getting call logs...")
                getCallLogs()
                Timber.i("Scraped ${callLogNumbers.size} numbers from call logs...")
            }
        }

        // TODO: custom compare merge to take the most recent timestamp one
        allNumbers = smsNumbers.union(callLogNumbers)
        Timber.i("Getting contact names for ${allNumbers.size} numbers...")
        getContactsNames()
    }

    private fun getSmsMessages() {

        val cursor = applicationContext.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf("DISTINCT ${Telephony.Sms.ADDRESS}", Telephony.Sms.DATE),
            "${Telephony.Sms.ADDRESS} IS NOT NULL) GROUP BY (${Telephony.Sms.ADDRESS}",
            null,
            "${Telephony.Sms.DATE} DESC"
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
                    val addressIndex: Int = getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                    val dateIndex: Int = getColumnIndexOrThrow(Telephony.Sms.DATE)
                    /*
                     * Moves to the next row in the cursor. Before the first movement in the
                     * cursor, the "row pointer" is -1, and if you try to retrieve data at that
                     * position you will get an exception.
                     */
                    moveToFirst()

                    do {
//						var msgData = ""
//						for (idx in 0 until cursor.columnCount) {
//							msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx)
//						}
//
//						Timber.i("MSGDATA: $msgData")

                        val number = PhoneNumberUtils.formatNumberToE164(
                            PhoneNumberUtils.normalizeNumber(getString(addressIndex)),
                            Locale.US.country
                        )

                        if (number != null) {
                            smsNumbers.add(Contact(number, getString(dateIndex)))
                        }
                    } while (moveToNext())
                }
                cursor.close()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCallLogs() {
        val seenNumbers: MutableSet<String> = mutableSetOf()
        val cursor = applicationContext.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE),
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
                    val numberIndex: Int = getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                    val dateIndex: Int = getColumnIndexOrThrow(CallLog.Calls.DATE)

                    moveToFirst()

                    do {
                        if (getString(numberIndex) != "") {
                            val number = PhoneNumberUtils.formatNumberToE164(
                                getString(numberIndex),
                                Locale.US.country
                            )

                            if (!seenNumbers.contains(number)) {
                                callLogNumbers.add(Contact(number, getString(dateIndex)))
                                seenNumbers.add(number)
                            }
                        }
                    } while (moveToNext())
                }
                cursor.close()
            }
        }
    }

    private fun getContactsNames() {
        allNumbers.forEach {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(it.number)
            )
            val cursor = applicationContext.contentResolver.query(
                uri,
                Array(1) { ContactsContract.PhoneLookup.DISPLAY_NAME },
                null,
                null,
                null
            )

            when (cursor?.count) {
                null -> {
                    Timber.e("Error fetching contact info for number $it")
                }
                0 -> {
                    //Timber.d("Unable to find contact for number $it.")
                }
                else -> {
                    cursor.apply {
                        val nameIndex: Int =
                            getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
                        moveToFirst()
                        if (getString(nameIndex) != "") {
                            conns.add(
                                Connection(
                                    name = getString(nameIndex),
                                    number = it.number,
                                    lastContact = it.contactDate
                                )
                            )
                        }
                    }
                }
            }
            cursor?.close()
        }
    }
}
