package com.example.map_umkm.model

data class MenuResponse(
    val menu_items: List<MenuDataItem>?
)

data class MenuItem(
    val id: Int,
    val category: String,
    val name: String,
    val description: String?,
    val image: String?,
    val created_at: String?,
    val price_hot: Int?,
    val price_iced: Int?
)