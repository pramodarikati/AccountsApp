package com.example.newsuserapp.di


import android.content.Context
import androidx.room.Room
import com.example.accountsapp.MyApplication
import com.example.accountsapp.data.local.AppDatabase
import com.example.accountsapp.data.local.Converters
import com.example.accountsapp.data.model.ItemDao
import com.example.accountsapp.network.ApiService
import com.example.accountsapp.network.RetrofitInstance
import com.example.accountsapp.repository.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContext(application: MyApplication): Context {
        return application.applicationContext
    }
    @Provides
    fun provideApiService(): ApiService = RetrofitInstance.apiService


    @Provides
    fun provideDao(db: AppDatabase): ItemDao = db.itemDao()

    @Provides
    fun provideRepository(apiService: ApiService, itemDao: ItemDao): MainRepository =
        MainRepository(apiService, itemDao)
}

