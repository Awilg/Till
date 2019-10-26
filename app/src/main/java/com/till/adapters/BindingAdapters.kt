package com.till.adapters

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

var format = SimpleDateFormat("MM/dd/yyyy", Locale.US)

@BindingAdapter("formatTimestamp")
fun formatTimestamp(view: TextView, timestamp: String) {
    view.text = format.format(Date(timestamp.toLong()))
}

@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(view.context)
            .load(imageUrl)
            .dontAnimate()
            .into(view)
    }
}