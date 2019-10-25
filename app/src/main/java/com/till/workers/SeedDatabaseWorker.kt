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
import com.till.util.mergeReduceInPlace
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class SeedDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private var smsMap: MutableMap<String, String> = mutableMapOf()
    private var numMap: MutableMap<String, String> = mutableMapOf()

    var conns: MutableList<Connection> = mutableListOf()

    override suspend fun doWork(): Result = coroutineScope {
        try {
            scrape()

            Timber.d("Connections made - size ${conns.size}")

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
                Timber.d("Getting sms numbers...")
                getSmsMessages()
                Timber.d("Scraped ${smsMap.size} numbers from sms messages...")
            }

            launch {
                Timber.d("Getting call logs...")
                getCallLogs()
                Timber.d("Scraped ${numMap.size} numbers from call logs...")
            }
        }

        Timber.d("Merging the lists...")

        // Take the most recent from the two maps
        numMap.mergeReduceInPlace(smsMap) { u, v -> if (u.toBigInteger() > v.toBigInteger()) u else v }

        Timber.d("Getting contact names for ${numMap.size} numbers...")
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

        when (cursor?.count) {
            null -> {
                Timber.e("Error fetching sms messages from provider")
            }
            0 -> {
                Timber.d("Sms message query failed. Try again.")
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
                            val date = getString(dateIndex)
                            smsMap.putIfAbsent(number, date)
                        }
                    } while (moveToNext())
                }
                cursor.close()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCallLogs() {
        val cursor = applicationContext.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE),
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )

        when (cursor?.count) {
            null -> {
                Timber.e("Error fetching sms messages from provider")
            }
            0 -> {
                Timber.d("Sms message query failed. Try again.")
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

                            numMap.putIfAbsent(number, getString(dateIndex))

                        }
                    } while (moveToNext())
                }
                cursor.close()
            }
        }
    }

    private fun getContactsNames() {
        numMap.forEach {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(it.key)
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
                    Timber.v("Unable to find contact for number $it.")
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
                                    number = it.key,
                                    lastContact = it.value
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
