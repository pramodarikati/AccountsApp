package com.example.accountsapp.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.accountsapp.data.model.ItemEntity
import com.example.accountsapp.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val reloadTrigger = MutableStateFlow(false)

    private val _itemDeleted = MutableLiveData<ItemEntity>()
    val itemDeleted: LiveData<ItemEntity> = _itemDeleted

    val items: Flow<PagingData<ItemEntity>> = searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            reloadTrigger.flatMapLatest {
                repository.getItemsPaging(query)
            }
        }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            val shouldFetch = repository.shouldFetchFromApi()
            if (shouldFetch) {
                val success = repository.refreshFromApi()
                if (success) reloadItems()
            } else {
                reloadItems()
            }
        }
    }

    fun update(item: ItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item)
            reloadItems()
        }
    }

    fun delete(item: ItemEntity, context: Context) {
        viewModelScope.launch {
            repository.deleteItem(item, context)
            reloadItems()
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun refreshFromApi() {
        viewModelScope.launch {
            val success = repository.refreshFromApi()
            if (success) reloadItems()
        }
    }

    fun refresh() {
        searchQuery.value = searchQuery.value

        viewModelScope.launch {
            if (repository.shouldFetchFromApi()) {
                repository.refreshFromApi()
            }
            reloadItems()
        }
    }

    private fun reloadItems() {
        reloadTrigger.value = !reloadTrigger.value
    }
}
