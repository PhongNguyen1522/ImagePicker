package com.phongnn.imagepicker.presenter

/*
* NOTE:
*   - Usage: Loading each image into image view
* */

import android.util.Log
import com.phongnn.imagepicker.data.api.ImageAPIService
import com.phongnn.imagepicker.data.api.RetrofitInstance
import com.phongnn.imagepicker.data.utils.APIConstantString
import com.phongnn.imagepicker.data.utils.CommonConstant
import com.phongnn.imagepicker.presenter.callback.ApiCallBack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import retrofit2.Response
import kotlin.Exception

class ImageLoaderImpl : ImageLoader {

    private lateinit var myApiService: ImageAPIService

    companion object {
        private var instance: ImageLoaderImpl? = null

        @JvmStatic
        fun getInstance(): ImageLoaderImpl {
            if (instance == null) {
                instance = ImageLoaderImpl()
            }
            return instance!!
        }
    }

    override fun loadImage(
        callBack: ApiCallBack,
    ) {

        // Create an api service
        myApiService = RetrofitInstance.getInstance().createRetrofit()

        // Call api for photo library
        runBlocking {
            launch(Dispatchers.IO) {
                try {
                    //Create retrofit
                    val parentBody = myApiService.getImageLibrary().body()
                    if (parentBody != null) {
                        val startLink = parentBody.startLink
                        Log.e(CommonConstant.MY_LOG_TAG, "startLink: $startLink")
                        val photoFramesList = parentBody.listPhotoFrames
                        if (photoFramesList.isNotEmpty()) {
                            for (photoFrame in photoFramesList) {
                                val folder = photoFrame.folder
                                val totalImage = photoFrame.totalImage
                                /*// Fake API
                                val totalImage = 49
                                val folder = "Birthday"*/
                                // Call API for each Images
                                for (frameNumber in 1..totalImage) {
                                    loadEachImage(
                                        APIConstantString.START_LINK,
                                        folder,
                                        frameNumber,
                                        callBack
                                    )
                                }
                            }
                        }

                    }

                } catch (e: Exception) {
                    Log.e(CommonConstant.MY_LOG_TAG, "${e.message}")
                }
            }
        }

        //Create retrofit
        /*
        val myApiService = RetrofitInstance.getInstance().createRetrofit()
        runBlocking {
            launch(Dispatchers.IO) {
                try {
                    val response: Response<ResponseBody> = myApiService.getChildImage(startLink,folder,frameNumber)
                    if (response.isSuccessful) {
                        val imgUrl = response.body()!!.bytes()
                        callBack.onApiCallingComplete(imgUrl)
                        Log.d(CommonConstant.MY_LOG_TAG, response.body()!!.string())
                    } else {
                        Log.e(CommonConstant.MY_LOG_TAG, "Call Api Failed!")
                    }
                } catch (e: Exception) {
                    Log.e(CommonConstant.MY_LOG_TAG, "${e.message}")
                }
            }
        }
        */
    }

    fun loadEachImage(startLink: String, folder: String, number: Int, callBack: ApiCallBack) {
        runBlocking {
            launch(Dispatchers.IO) {
                try {
                    val response: Response<ResponseBody> =
                        myApiService.getChildImage(startLink, folder, number)
                    if (response.isSuccessful) {
                        val imgUrl = response.body()!!.bytes()
                        callBack.onApiCallingComplete(imgUrl)
                        Log.d(CommonConstant.MY_LOG_TAG, "response success!!!")
                    } else {
                        Log.e(CommonConstant.MY_LOG_TAG, "Call Api Failed!")
                    }
                } catch (e: Exception) {
                    Log.e(CommonConstant.MY_LOG_TAG, "${e.message}")
                }
            }
        }
    }
}