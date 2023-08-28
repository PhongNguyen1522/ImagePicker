package com.phongnn.imagepicker.data.dbentity.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_table")
data class ImageEntity(
    @PrimaryKey
    val id: Int = 0, // Position in Recycler View
    val imageUrl: ByteArray,
    val isDownLoaded: Boolean
) {

    // Auto generate
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageEntity

        if (id != other.id) return false
        if (!imageUrl.contentEquals(other.imageUrl)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + imageUrl.contentHashCode()
        return result
    }
}