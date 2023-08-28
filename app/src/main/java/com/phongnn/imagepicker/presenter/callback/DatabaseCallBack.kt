package com.phongnn.imagepicker.presenter.callback

import com.phongnn.imagepicker.data.dbentity.entity.ImageEntity
import com.phongnn.imagepicker.data.model.MyImage

interface DatabaseCallBack {

    // Listen selected images to download
    fun onImageSelected(savingImage: ImageEntity)

    // Listen all users list
    fun onAllImagesReturn(allUsers: List<ImageEntity>)
}