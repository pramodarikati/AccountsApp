package com.example.accountsapp.data.model

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*


@Dao
interface ItemDao {

    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getAllItems(): LiveData<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<ItemEntity>)

    @Update
    fun update(item: ItemEntity)

    @Delete
    fun delete(item: ItemEntity)

    @Query("DELETE FROM items")
    fun clearAll()

    @Query("SELECT * FROM items ORDER BY id ASC")
    fun getPagingSource(): PagingSource<Int, ItemEntity>

    @Query("SELECT COUNT(*) FROM items")
    fun getItemCount(): Int

    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' ORDER BY id DESC")
    fun getItemsPaging(query: String): PagingSource<Int, ItemEntity>
}






