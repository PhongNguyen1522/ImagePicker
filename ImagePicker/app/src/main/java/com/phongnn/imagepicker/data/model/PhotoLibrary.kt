package com.phongnn.imagepicker.data.model


import com.google.gson.annotations.SerializedName

data class PhotoLibrary(
    @SerializedName("listPhotoFrames")
    val listPhotoFrames: List<PhotoFrames>,
    @SerializedName("start_link")
    val startLink: String
)