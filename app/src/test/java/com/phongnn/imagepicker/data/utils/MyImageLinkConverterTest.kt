package com.phongnn.imagepicker.data.utils

import android.graphics.BitmapFactory
import android.util.Log
import com.bumptech.glide.Glide
import com.google.common.truth.Truth.assertThat
import com.phongnn.imagepicker.data.api.ImageAPIService
import com.phongnn.imagepicker.data.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.properties.Delegates

class MyImageLinkConverterTest {

    private lateinit var service: ImageAPIService
    private lateinit var imageLinkConverter: ImageLinkConverter
    private var cnt by Delegates.notNull<Int>()

    @Before
    fun setUp() {
        cnt = 0
        service = RetrofitInstance.getInstance().createRetrofit()
        imageLinkConverter =
            ImageLinkConverter()
    }

    @Test
    fun imagePathTest(){
//        val folderCoverImage = imageLinkConverter.getChildImagePath()
//        assertThat(folderCoverImage).isNotEmpty()
    }

    @Test
    fun callAPITest() {
        runBlocking {
            val resBody = service.getImageLibrary().body()
            // Try to load images
            val startLink = resBody!!.startLink
            val photoFramesList = resBody.listPhotoFrames
            while (photoFramesList.isNotEmpty()) {
                for (photoFrame in photoFramesList) {
                    val folder = photoFrame.folder
                    val totalImage = photoFrame.totalImage
                    // Call API for each Images
                    for (frameNumber in 1..totalImage) {
                        loadImage(startLink, folder, frameNumber)
                        cnt++
                    }
                }
            }
            assertThat(cnt).isGreaterThan(1000)
        }
    }


    private fun loadImage(startLink: String, folder: String, frameNumber: Int) {
        //Create retrofit
        val myApiService = RetrofitInstance.getInstance().createRetrofit()
        runBlocking {
            launch(Dispatchers.IO) {
                try {
                    val response: Response<ResponseBody> = myApiService.getChildImage(startLink,folder,frameNumber)
                    if (response.isSuccessful) {
                        val imgUrl = response.body()!!.string()
//                        val imgUrl = response.body()!!.bytes()
                        Log.d(CommonConstant.MY_LOG_TAG, imgUrl)
                    } else {
//                        Log.e(CommonConstant.MY_LOG_TAG, "Call Api Failed!")
                    }
                } catch (e: java.lang.Exception) {
//                    Log.e(CommonConstant.MY_LOG_TAG, "${e.message}")
                }
            }
        }
    }
}