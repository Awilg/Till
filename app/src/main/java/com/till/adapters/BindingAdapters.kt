package com.till.adapters

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*

var format = SimpleDateFormat("MM/dd/yyyy", Locale.US)

@BindingAdapter("formatTimestamp")
fun formatTimestamp(view: TextView, timestamp: String) {
    view.text = format.format(Date(timestamp.toLong()))
}