package com.phongnn.imagepicker.data.api

import com.phongnn.imagepicker.data.model.PhotoLibrary
import com.phongnn.imagepicker.data.utils.APIConstantString
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ImageAPIService {

    @GET(APIConstantString.GET_IMAGES_LINK)
    suspend fun getImageLibrary(): Response<PhotoLibrary>

    @GET("{start_link}{folder}/{folder}_frame_{frameNumber}.png")
    suspend fun getChildImage(
        @Path("start_link")
        startLink: String,
        @Path("folder")
        folder: String,
        @Path("frameNumber")
        frameNumber: Int
    ): Response<ResponseBody>

}