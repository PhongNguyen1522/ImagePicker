package com.phongnn.imagepicker.presenter

import android.content.Context
import com.jsibbold.zoomage.ZoomageView
import com.phongnn.imagepicker.data.model.ImageInfo
import com.phongnn.imagepicker.data.model.MyImage
import com.phongnn.imagepicker.data.model.Song
import com.phongnn.imagepicker.presenter.callback.ApiCallBack

interface ImageLoader {
    fun loadImage(callBack: ApiCallBack)
    // Return download Id
    fun downLoadImageToStorage(context: Context, myImage: MyImage): Long

    // Get all images from local storage
    fun getAllImagesFromLocalStorage(folderPath: String): List<ImageInfo>

    fun getAllMusicFromLocalStorage(context: Context, folderPath: String): List<Song>
}