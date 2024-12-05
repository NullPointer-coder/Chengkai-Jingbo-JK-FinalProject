package com.example.smartreciperecommenderapp.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(private val categoryDao: CategoryDao) : ViewModel() {
    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories

    init {
        loadCategories() // ViewModel 初始化时加载数据
    }

    fun loadCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _categories.value = categoryDao.getAllCategories()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveCategories(newCategories: List<CategoryEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                categoryDao.clearCategories()
                categoryDao.insertCategories(newCategories)
                _categories.value = categoryDao.getAllCategories()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

