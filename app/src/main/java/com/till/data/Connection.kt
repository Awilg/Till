package com.till.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connections")
data class Connection(
    val name: String,
    @PrimaryKey val number: String,
    val lastContact: String,
    val contactId: String,
    val contactPhotoUri: String?,
    val contactThumbnailUri: String?,
    val description: String? = "",
    val contactInterval: Int = 30 // how often the person should be contacted
)
