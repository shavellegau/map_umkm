package com.example.map_umkm.data

import android.content.Context
import com.example.map_umkm.model.MenuData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.IOException

class JsonHelper(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val MENU_FILE_NAME = "menu_items.json"

    fun getMenuData(): MenuData? {
        val internalFile = File(context.filesDir, MENU_FILE_NAME)

        return try {
            val jsonString = if (internalFile.exists()) {
                internalFile.readText()
            } else {
                
                val textFromAssets = context.assets.open(MENU_FILE_NAME).bufferedReader().use { it.readText() }
                internalFile.writeText(textFromAssets)
                textFromAssets
            }

            
            val menuData = gson.fromJson(jsonString, MenuData::class.java)
            if (menuData.orders == null) {
                menuData.orders = mutableListOf()
            }
            menuData

        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun saveMenuData(menuData: MenuData) {
        try {
            val file = File(context.filesDir, MENU_FILE_NAME)
            file.writer().use { writer ->
                gson.toJson(menuData, writer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    
    fun addOrder(newOrder: com.example.map_umkm.model.Order): Boolean {
        return try {
            val menuData = getMenuData() ?: return false
            menuData.orders.add(0, newOrder) 
            saveMenuData(menuData)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    
    fun updateOrderStatus(orderId: String, newStatus: String): Boolean {
        return try {
            val menuData = getMenuData() ?: return false
            val orderToUpdate = menuData.orders.find { it.orderId == orderId }
            if (orderToUpdate != null) {
                orderToUpdate.status = newStatus
                saveMenuData(menuData)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
