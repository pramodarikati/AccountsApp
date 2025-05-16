package com.example.accountsapp.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val name: String,
    val details: String?
)
