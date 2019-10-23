package com.till.workers

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.till.data.AppDatabase
import com.till.data.Connection
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class SeedDatabaseWorker(
	context: Context,
	workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
	var numbers: Set<String> = emptySet()
	var names: Set<String> = emptySet()
	var calls: Set<String> = emptySet()

	var conns : List<Connection> = emptyList()

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
				Timber.i("Scraped ${numbers.size} numbers from sms messages...")
			}

			launch {
				Timber.i("Getting call logs...")
				getCallLogs()
			}
		}

		getContactsNames()
	}

	private fun getSmsMessages() {
		// public static final String INBOX = "content://sms/inbox";
		// public static final String SENT = "content://sms/sent";
		// public static final String DRAFT = "content://sms/draft";
		val cursor = applicationContext.contentResolver.query(
			Telephony.Sms.CONTENT_URI,
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

	@SuppressLint("MissingPermission")
	private fun getCallLogs() {
		val cursor = applicationContext.contentResolver.query(
			CallLog.Calls.CONTENT_URI,
			Array(1) { CallLog.Calls.NUMBER },
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
					val index: Int = getColumnIndex(CallLog.Calls.NUMBER)

					moveToFirst()

					do {
						calls = calls.plus(getString(index))
					} while (moveToNext())
				}
				cursor.close()
			}
		}

		Timber.i("Calls:  $calls")
	}

	private fun getContactsNames() {
		numbers.forEach {
			val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(it))
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
					//Timber.i("Unable to find contact for number $it.")
				}
				else -> {
					cursor.apply {
						val nameIndex: Int = getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
						moveToFirst()
						conns = conns.plus(Connection(name = getString(nameIndex), number = it))
					}
				}
			}
			cursor?.close()
		}
	}
}
