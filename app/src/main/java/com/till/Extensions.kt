package com.till

import android.app.Activity
import android.content.ContentResolver
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

fun Fragment.contentResolver(): ContentResolver {
	return (activity as Activity).contentResolver
}

var fullFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
var shortFormat = SimpleDateFormat("MM/dd", Locale.US)

fun String.fromTimestampToFormatMonthDay(): String {
	return shortFormat.format(Date(this.toLong()))
}

fun String.fromTimestampToFormatMonthDayYear(): String {
	return fullFormat.format(Date(this.toLong()))
}