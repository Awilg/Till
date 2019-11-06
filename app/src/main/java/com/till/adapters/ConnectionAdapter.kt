package com.till.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.till.data.Connection
import com.till.databinding.ConnectionCardBinding

class ConnectionAdapter(private val connectionListener: ConnectionListener) :
    ListAdapter<Connection, RecyclerView.ViewHolder>(ConnectionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ConnectionViewHolder(
            ConnectionCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), connectionListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val connection = getItem(position)
        (holder as ConnectionViewHolder).bind(connection)
    }

    class ConnectionViewHolder(
        private val binding: ConnectionCardBinding,
        private val connectionListener: ConnectionListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Connection) {
            binding.connection = item

            binding.root.setOnClickListener { connectionListener.navigateToConnectionFragment() }
            binding.profileImage.setOnClickListener { connectionListener.favoriteConnection() }
            binding.executePendingBindings()
        }
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