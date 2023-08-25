package com.phongnn.imagepicker.data.api

import com.phongnn.imagepicker.data.utils.APIConstantString
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        private var instance: RetrofitInstance? = null
        @JvmStatic
        fun getInstance(): RetrofitInstance {
            if (instance == null) {
                instance = RetrofitInstance()
            }
            return instance!!
        }
    }

    fun createRetrofit(): ImageAPIService {
        val myApi: ImageAPIService by lazy {
            Retrofit.Builder()
                .baseUrl(APIConstantString.MAIN_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ImageAPIService::class.java)
        }
        return myApi
    }
}