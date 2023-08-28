package com.phongnn.imagepicker.presenter

import android.annotation.SuppressLint
import android.widget.ImageView
import com.phongnn.imagepicker.data.dbentity.entity.ImageEntity
import com.phongnn.imagepicker.data.repo.ImageRepository

class ImagePresenter private constructor(private val imageRepository: ImageRepository) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: ImagePresenter? = null
        @JvmStatic
        fun getInstance(imageRepository: ImageRepository): ImagePresenter {
            if (instance == null) {
                instance = ImagePresenter(imageRepository)
            }
            return instance!!
        }
    }

    private var view: ImageView? = null

    fun attachView(view: ImageView) {
        this.view = view
    }

    fun detachView() {
        this.view = null
    }

    suspend fun insertImage(image: ImageEntity) {
        imageRepository.insertImage(image)
    }

    suspend fun getImageById(id: Int): ImageEntity? {
        return imageRepository.getImageById(id)
    }

    suspend fun getAllUsers(): List<ImageEntity> {
        return imageRepository.getAllImages()
    }

    suspend fun deleteAllImages() {
        return imageRepository.deleteAllImages()
    }
    
}