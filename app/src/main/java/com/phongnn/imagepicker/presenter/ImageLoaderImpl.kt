package com.phongnn.imagepicker.presenter

/*
* NOTE:
*   - Usage: Loading each image into image view
* */

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.phongnn.imagepicker.data.api.ImageAPIService
import com.phongnn.imagepicker.data.api.RetrofitInstance
import com.phongnn.imagepicker.data.dbentity.db.ImageDatabase
import com.phongnn.imagepicker.data.dbentity.entity.ImageEntity
import com.phongnn.imagepicker.data.model.MyImage
import com.phongnn.imagepicker.data.repo.ImageRepository
import com.phongnn.imagepicker.data.utils.APIConstantString
import com.phongnn.imagepicker.data.utils.CommonConstant
import com.phongnn.imagepicker.data.utils.ImageLinkConverter
import com.phongnn.imagepicker.presenter.callback.ApiCallBack
import com.phongnn.imagepicker.presenter.callback.DatabaseCallBack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import retrofit2.Response

class ImageLoaderImpl(context: Context) : ImageLoader {

    private lateinit var myApiService: ImageAPIService
    private var imagePresenter: ImagePresenter
    private lateinit var myImage: MyImage

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var instance: ImageLoaderImpl? = null

        @JvmStatic
        fun getInstance(context: Context): ImageLoaderImpl {
            if (instance == null) {
                instance = ImageLoaderImpl(context)
            }
            return instance!!
        }
    }

    init {
        imagePresenter = ImagePresenter.getInstance(ImageRepository(ImageDatabase.getDatabase(context).imageDao()))
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
                        val photoFramesList = parentBody.listPhotoFrames
                        if (photoFramesList.isNotEmpty()) {
                            for (photoFrame in photoFramesList) {
                                val folder = photoFrame.folder
                                callBack.onImageTopicReturn(folder)
                                val totalImage = photoFrame.totalImage
                                // Call API for each Images
                                for (frameNumber in 1..3) {
                                    // Link Uri for call images
                                    val myUri = ImageLinkConverter.getInstance().getChildImagePath(
                                        APIConstantString.START_LINK_FULL_FOR_DOWNLOAD,
                                        folder,
                                        frameNumber
                                    )
                                    loadEachImage(
                                        APIConstantString.START_LINK,
                                        folder,
                                        frameNumber,
                                        callBack,
                                        myUri
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
    }

    private fun loadEachImage(
        newStartLink: String,
        folder: String,
        number: Int,
        callBack: ApiCallBack,
        myUri: String,
    ) {
        runBlocking {
            launch(Dispatchers.IO) {
                try {
                    val response: Response<ResponseBody> =
                        myApiService.getChildImage(newStartLink, folder, number)
                    // When success, save into MyImage Object and update recycler view
                    if (response.isSuccessful) {
                        val imgUrl = response.body()!!.bytes()
                        myImage = MyImage(imgUrl, myUri, false)

                        // Callback for save and update
                        callBack.onImageReturn(myImage)

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

    override fun downloadImage(context: Context, imageEntity: ImageEntity) {
        try {
            val request = DownloadManager.Request(Uri.parse(myImage.uri))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setTitle("Image Download")
                .setDescription("Downloading image ...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "image.jpg")
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            Log.e(CommonConstant.MY_LOG_TAG, e.message.toString())
        }

        runBlocking {
            launch(Dispatchers.IO) {
                imagePresenter.insertImage(imageEntity)
            }
        }
    }

    override fun showImageById(context: Context, imageEntity: ImageEntity) {
        runBlocking {
            launch(Dispatchers.IO) {
                imagePresenter.getImageById(imageEntity.id)
            }
        }
    }

    override fun showAllImages(callback: DatabaseCallBack) {
        runBlocking {
            launch(Dispatchers.IO) {
                callback.onAllImagesReturn(imagePresenter.getAllUsers())
            }
        }
    }

    override fun deleteAllImages() {
        runBlocking {
            launch(Dispatchers.IO) {
                imagePresenter.deleteAllImages()
            }
        }
    }


}