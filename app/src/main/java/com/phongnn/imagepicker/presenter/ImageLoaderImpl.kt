package com.phongnn.imagepicker.presenter

/*
* NOTE:
*   - Usage: Loading each image into image view
* */

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.phongnn.imagepicker.data.api.ImageAPIService
import com.phongnn.imagepicker.data.api.RetrofitInstance
import com.phongnn.imagepicker.data.model.ImageInfo
import com.phongnn.imagepicker.data.model.MyImage
import com.phongnn.imagepicker.data.model.Song
import com.phongnn.imagepicker.data.utils.APIConstantString
import com.phongnn.imagepicker.data.utils.CommonConstant
import com.phongnn.imagepicker.data.utils.ImageLinkConverter
import com.phongnn.imagepicker.presenter.callback.ApiCallBack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File


class ImageLoaderImpl(context: Context) : ImageLoader {

    private lateinit var myApiService: ImageAPIService
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
                                for (frameNumber in 1..5) {
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
                        val imageName = "${folder}_${number}.jpg"
                        myImage = MyImage(imageName, imgUrl, myUri)

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


    override fun downLoadImageToStorage(context: Context, myImage: MyImage): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(myImage.uri))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setTitle("Image Download")
            .setDescription("Downloading image ...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        val downloadDirectory = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ).toString()
        val folderName = "ImagePicker"
        val fileName = myImage.imageName
        val subFolderPath = "$downloadDirectory/$folderName"
        val subFolder = File(subFolderPath)
        if (!subFolder.exists()) {
            subFolder.mkdir()
        }
        // Set dest folder and file name
        val destFilePath = "$subFolderPath/$fileName"

        request.setDestinationUri(Uri.parse("file://$destFilePath"))

        val id = downloadManager.enqueue(request)

        Log.d(CommonConstant.MY_LOG_TAG, "Download ID: $id")
        return id
    }

    override fun getAllImagesFromLocalStorage(folderPath: String): List<ImageInfo> {
        val imgInfoList = mutableListOf<ImageInfo>()

        val folder = File(folderPath)
        if (folder.isDirectory) {
            val fileList = folder.listFiles()
            for (file in fileList) {
                if (file.isFile) {
                    ImageInfo(file.name, Uri.fromFile(file)).also {
                        imgInfoList.add(it)
                    }
                }
            }
        }

        return imgInfoList
    }

    override fun getAllMusicFromLocalStorage(context: Context, folderPath: String): List<Song> {
        val musicInfoList = mutableListOf<Song>()

        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)
                val duration = it.getLong(durationColumn)
                val data = it.getString(dataColumn)

                val contentUri = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString()
                )

                musicInfoList.add(
                    Song(
                        id,
                        title,
                        artist,
                        duration,
                        contentUri,
                        data
                    )
                )
            }
        }

        return musicInfoList
    }


}