package com.example.appmapsedwin.mvvm.repository

import com.example.appmapsedwin.mvvm.repository.ApiService
import com.example.appmapsedwin.mvvm.model.LocationData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LocationApi {
    @POST("tu_endpoint")
    suspend fun sendLocation(@Body locationData: LocationData): Response<ResponseBody>
}