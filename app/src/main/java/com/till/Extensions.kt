package com.till

import android.app.Activity
import android.content.ContentResolver
import androidx.fragment.app.Fragment

fun Fragment.contentResolver(): ContentResolver {
	return (activity as Activity).contentResolver
}
