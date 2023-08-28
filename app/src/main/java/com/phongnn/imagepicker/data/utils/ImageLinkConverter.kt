package com.phongnn.imagepicker.data.utils

class ImageLinkConverter() {

    companion object {
        private var instance: ImageLinkConverter? = null

        @JvmStatic
        fun getInstance(): ImageLinkConverter {
            if (instance == null) {
                instance = ImageLinkConverter()
            }
            return instance!!
        }
    }

    fun getChildImagePath(startLink: String, folder: String, frameNumber: Int): String {
        return "$startLink$folder/${folder}_frame_${frameNumber}.png"
    }
}
