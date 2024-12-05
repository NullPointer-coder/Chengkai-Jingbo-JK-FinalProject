package com.example.smartreciperecommenderapp.data.model

import java.util.Date

data class Ingredient(
    val id: Int = 0,                        // 唯一标识符
    val name: String = "Unknown",           // 食材名称
    val quantity: Double = 0.0,             // 数量
    val unit: String = "Unknown",           // 单位
    val category: String = "General",       // 类别
    val expiryDate: Date? = null,           // 到期日期 (可选)
    val imageUrl: String? = null            // 图片链接 (可选)
)
