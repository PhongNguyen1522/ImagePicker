package com.phongnn.imagepicker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.phongnn.imagepicker.data.api.RetrofitInstance
import com.phongnn.imagepicker.data.utils.APIConstantString
import com.phongnn.imagepicker.data.utils.CommonConstant
import com.phongnn.imagepicker.databinding.ActivityMainBinding
import com.phongnn.imagepicker.presenter.ImageLoader
import com.phongnn.imagepicker.presenter.ImageLoaderImpl
import com.phongnn.imagepicker.presenter.callback.ApiCallBack
import com.phongnn.imagepicker.ui.adapter.ChildImageAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageLoader: ImageLoader
    private var imagesList = mutableListOf<ByteArray>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init Recycler View
        binding.rcvImages.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        // Set Adapter
        val childImageAdapter = ChildImageAdapter(imagesList)
        binding.rcvImages.adapter = childImageAdapter

        runBlocking {
            launch(Dispatchers.IO) {
                loadingImage(object : ApiCallBack {
                    override fun onApiCallingComplete(imgUrl: ByteArray) {
                        // Save all images into a List
                        var tempList = mutableListOf<ByteArray>()
                        tempList.add(imgUrl)
                        imagesList.addAll(tempList)
                    }
                })
            }
        }
    }


    private suspend fun loadingImage(apiCallBack: ApiCallBack) {
        imageLoader = ImageLoaderImpl.getInstance().also {
            it.loadImage(apiCallBack)
        }
    }

}