package com.phongnn.imagepicker.data.repo

import com.phongnn.imagepicker.data.dbentity.dao.ImageDao
import com.phongnn.imagepicker.data.dbentity.entity.ImageEntity

class ImageRepository(private val imageDao: ImageDao) {

    suspend fun insertImage(image: ImageEntity) {
        imageDao.insertImage(image)
    }

    suspend fun getImageById(imageId: Int): ImageEntity? {
        return imageDao.getImageById(imageId)
    }

    suspend fun getAllImages(): List<ImageEntity> {
        return imageDao.getAllImages()
    }

    suspend fun deleteAllImages() {
        return imageDao.deleteAllImages()
    }

}