package com.till.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.till.data.Connection
import com.till.databinding.ConnectionCardBinding

class ConnectionAdapter :
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
        init {
            binding.setClickListener {
                //                binding.connection?.let { connection ->
//                    binding.
//                    //navigateToPlant(connection, it)
//                }
            }
        }

//        private fun navigateToPlant(
//            plant: Plant,
//            it: View
//        ) {
//            val direction =
//                HomeViewPagerFragmentDirections.actionViewPagerFragmentToPlantDetailFragment(
//                    plant.plantId
//                )
//            it.findNavController().navigate(direction)
//        }

        fun bind(item: Connection) {
            binding.apply {
                connection = item
                executePendingBindings()
            }
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