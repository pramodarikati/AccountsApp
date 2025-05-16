package com.example.accountsapp.network

import com.example.accountsapp.data.model.ApiItem
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("objects")
    suspend fun getItems(): Response<List<ApiItem>>
}

