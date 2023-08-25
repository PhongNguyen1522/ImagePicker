package com.phongnn.imagepicker.data.model


import com.google.gson.annotations.SerializedName

data class Define(
    @SerializedName("end")
    val end: Int,
    @SerializedName("indexDefineCollage")
    val indexDefineCollage: Int,
    @SerializedName("start")
    val start: Int,
    @SerializedName("totalCollageItemContainer")
    val totalCollageItemContainer: Int
)