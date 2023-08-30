package com.phongnn.imagepicker.presenter.callback

import com.phongnn.imagepicker.data.model.MyImage

interface ApiCallBack {

    // Listen image topic
    fun onImageTopicReturn(topic: String)

    // Listen Image Object
    fun onImageReturn(returnImage: MyImage)

}