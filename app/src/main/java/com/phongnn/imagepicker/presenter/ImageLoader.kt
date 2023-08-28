package com.phongnn.imagepicker.presenter

import android.content.Context
import com.phongnn.imagepicker.data.dbentity.entity.ImageEntity
import com.phongnn.imagepicker.data.model.MyImage
import com.phongnn.imagepicker.presenter.callback.ApiCallBack
import com.phongnn.imagepicker.presenter.callback.DatabaseCallBack

interface ImageLoader {
    fun loadImage(callBack: ApiCallBack)
    fun downloadImage(context: Context, imageEntity: ImageEntity)
    fun showImageById(context: Context, imageEntity: ImageEntity)
    fun showAllImages(callback: DatabaseCallBack)
    fun deleteAllImages()
}