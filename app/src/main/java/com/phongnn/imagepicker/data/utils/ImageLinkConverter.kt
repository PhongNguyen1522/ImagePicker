package com.phongnn.imagepicker.data.utils

class ImageLinkConverter(
    private val startLink: String,
    private val folder: String,
    private val frameNumber: Int,
) {


    companion object {
        private var instance: ImageLinkConverter? = null

        @JvmStatic
        fun getInstance(newStartLink: String, newFolder: String, newFrameNumber: Int): ImageLinkConverter {
            if (instance == null) {
                instance = ImageLinkConverter(newStartLink, newFolder,newFrameNumber)
            }
            return instance!!
        }
    }

    fun getChildImagePath(): String {
        return "$startLink$folder/${folder}_frame_${frameNumber}.png"
    }
}
