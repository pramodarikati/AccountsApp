package com.example.accountsapp.data.model


data class ApiResponse(
    val id: String,
    val name: String,
    val data: Map<String, Any>?
)
