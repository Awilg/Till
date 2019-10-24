package com.till.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*
import java.util.Calendar.DAY_OF_YEAR

@Entity(tableName = "connections")
data class Connection(
    val name: String,
    @PrimaryKey val number: String,
    val description: String = "",
    val lastContact: String,
    val contactInterval: Int = 30, // how often the person should be contacted
    val imageUrl: String = ""
) {

	/**
	 * Determines if the the Connection should be contacted.  Returns true if [since]'s date > date of last
	 * contact + contact Interval; false otherwise.
	 */
	fun contactRequired(since: Calendar, lastContactDate: Calendar) =
		since > lastContactDate.apply { add(DAY_OF_YEAR, contactInterval) }

	override fun toString() = name
}
