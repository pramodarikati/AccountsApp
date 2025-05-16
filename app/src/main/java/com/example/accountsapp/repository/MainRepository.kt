package com.example.accountsapp.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.accountsapp.data.model.ApiItem
import com.example.accountsapp.data.model.ItemDao
import com.example.accountsapp.data.model.ItemEntity
import com.example.accountsapp.network.ApiService
import com.example.accountsapp.network.RetrofitInstance
import com.example.accountsapp.util.Prefs
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import javax.inject.Inject
import retrofit2.Callback
import retrofit2.Response


class MainRepository @Inject constructor(
    private val apiService: ApiService,
    private val itemDao: ItemDao
) {

    fun getAllItems(): LiveData<List<ItemEntity>> = itemDao.getAllItems()

    fun deleteItem(item: ItemEntity, context: Context) = CoroutineScope(Dispatchers.IO).launch {
        itemDao.delete(item)

        if (Prefs.areNotificationsEnabled(context)) {
            withContext(Dispatchers.Main) {
                val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else true

                if (hasPermission) {
                    NotificationUtils.showNotification(
                        context,
                        "Item Deleted",
                        "Deleted item: ${item.name} (ID: ${item.id})"
                    )
                }
            }
        }

    }


    fun updateItem(item: ItemEntity) = CoroutineScope(Dispatchers.IO).launch {
        itemDao.update(item)
    }

    suspend fun shouldFetchFromApi(): Boolean = withContext(Dispatchers.IO) {
        itemDao.getItemCount() == 0
    }

    fun getItemsPaging(query: String): Flow<PagingData<ItemEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { itemDao.getItemsPaging(query) }
        ).flow
    }

    suspend fun refreshFromApi(): Boolean {
        return try {
            val response = apiService.getItems()
            if (response.isSuccessful) {
                val apiItems = response.body() ?: emptyList()
                val entities = apiItems.map {
                    ItemEntity(it.id, it.name, it.data?.toString() ?: "No details")
                }
                withContext(Dispatchers.IO) {
                    itemDao.clearAll()
                    itemDao.insertAll(entities)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("MainRepository", "API error: ${e.message}")
            false
        }
    }


    fun getAllItemsPaged(): Flow<PagingData<ItemEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { itemDao.getPagingSource() }
        ).flow
    }
}

