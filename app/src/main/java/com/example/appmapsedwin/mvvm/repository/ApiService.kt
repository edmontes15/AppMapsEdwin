package com.example.appmapsedwin.mvvm.repository


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://mail.google.com/mail/u/0/#inbox")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val locationApi: LocationApi by lazy {
        retrofit.create(LocationApi::class.java)
    }
}