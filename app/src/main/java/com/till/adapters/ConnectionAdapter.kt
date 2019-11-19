package com.till.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.till.R
import com.till.data.Connection
import com.till.databinding.ConnectionCardBinding
import java.util.*

class ConnectionAdapter(
    private var _connectionList: MutableList<Connection>? = null
) :
    ListAdapter<Connection, RecyclerView.ViewHolder>(ConnectionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ConnectionViewHolder(
            ConnectionCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val connection = getItem(position)
        (holder as ConnectionViewHolder).bind(connection)
    }

    class ConnectionViewHolder(
        private val binding: ConnectionCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Connection) {
            binding.connection = item
            binding.expandActionButtons.visibility = View.GONE
            binding.root.setOnClickListener {
                if (binding.expandActionButtons.visibility == View.VISIBLE) {
                    binding.expandActionButtons.visibility = View.GONE
                } else {
                    binding.expandActionButtons.visibility = View.VISIBLE
                }
            }
            binding.actionText.setOnClickListener {
                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data =
                        Uri.parse("smsto:${item.number}")  // This ensures only SMS apps respond
                    putExtra("sms_body", binding.root.context.getString(R.string.sms_template_omg))
                }
                startActivity(binding.root.context, smsIntent, null)
            }
            binding.actionCall.setOnClickListener {

                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${item.number}")
                }
                startActivity(binding.root.context, callIntent, null)
            }
            binding.executePendingBindings()
        }
    }

    override fun submitList(list: MutableList<Connection>?) {
        _connectionList = list
        super.submitList(list)
    }

    fun filter(text: String) {
        val filtered = mutableListOf<Connection>()
        if (text.isEmpty()) {
            super.submitList(_connectionList)
        } else {
            val lowerText = text.toLowerCase(Locale.getDefault())
            _connectionList?.let { it ->
                it.stream().forEach { conn ->
                    if (
                        conn.name.toLowerCase(Locale.getDefault()).contains(lowerText) ||
                        conn.number.toLowerCase(Locale.getDefault()).contains(lowerText)
                    ) {
                        filtered.add(conn)
                    }
                }
            }
            super.submitList(filtered)
        }
        notifyDataSetChanged()
    }

}


private class ConnectionDiffCallback : DiffUtil.ItemCallback<Connection>() {

    override fun areItemsTheSame(oldItem: Connection, newItem: Connection): Boolean {
        return oldItem.contactId == newItem.contactId
    }

    override fun areContentsTheSame(oldItem: Connection, newItem: Connection): Boolean {
        return oldItem == newItem
    }
}