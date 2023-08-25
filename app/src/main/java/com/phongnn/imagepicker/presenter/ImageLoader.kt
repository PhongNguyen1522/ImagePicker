package com.phongnn.imagepicker.presenter

import com.phongnn.imagepicker.presenter.callback.ApiCallBack

interface ImageLoader {
    fun loadImage(callBack: ApiCallBack)
}