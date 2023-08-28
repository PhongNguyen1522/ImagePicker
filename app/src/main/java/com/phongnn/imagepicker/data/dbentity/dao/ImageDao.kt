package com.phongnn.imagepicker.data.dbentity.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.phongnn.imagepicker.data.dbentity.entity.ImageEntity

@Dao
interface ImageDao {
    @Insert
    suspend fun insertImage(image: ImageEntity)

    @Query("SELECT * FROM image_table WHERE id = :imageId")
    suspend fun getImageById(imageId: Int): ImageEntity?

    @Query("SELECT * FROM image_table")
    suspend fun getAllImages(): List<ImageEntity>

    @Query("DELETE FROM image_table")
    suspend fun deleteAllImages()

}