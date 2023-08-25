package com.phongnn.imagepicker.data.model


import com.google.gson.annotations.SerializedName

data class PhotoFrames(
    @SerializedName("cover")
    val cover: String,
    @SerializedName("defines")
    val defines: List<Define>,
    @SerializedName("folder")
    val folder: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("lock")
    val lock: Boolean,
    @SerializedName("name")
    val name: String,
    @SerializedName("name_vi")
    val nameVi: String,
    @SerializedName("openPackageName")
    val openPackageName: String,
    @SerializedName("totalImage")
    val totalImage: Int
)