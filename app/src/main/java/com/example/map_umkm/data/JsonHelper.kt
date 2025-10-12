package com.example.map_umkm.data

import android.content.Context
import com.example.map_umkm.model.MenuData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.IOException

/**
 * Kelas ini berfungsi sebagai "database" lokal untuk aplikasi.
 * Semua operasi baca dan tulis (read/write) ke file JSON ditangani di sini.
 */
class JsonHelper(private val context: Context) {

    // Inisialisasi library GSON. setPrettyPrinting() membuat format file JSON menjadi rapi dan mudah dibaca manusia.
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    // Nama file konstanta agar tidak ada salah ketik di tempat lain.
    private val MENU_FILE_NAME = "menu_items.json"
    private val ORDERS_FILE_NAME = "orders.json"
    private val VOUCHERS_FILE_NAME = "vouchers.json"


    fun getMenuData(): MenuData? {
        val internalFile = File(context.filesDir, MENU_FILE_NAME)

        return try {
            if (internalFile.exists()) {
                // Jika file sudah ada di internal storage, baca dari sana.
                internalFile.reader().use { reader ->
                    gson.fromJson(reader, MenuData::class.java)
                }
            } else {
                // Jika tidak ada, baca dari 'assets' sebagai file default.
                context.assets.open(MENU_FILE_NAME).bufferedReader().use { reader ->
                    val data = gson.fromJson(reader, MenuData::class.java)
                    // Langsung salin ke internal storage agar bisa diedit nanti.
                    saveMenuData(data)
                    data
                }
            }
        } catch (e: IOException) {
            e.printStackTrace() // Cetak error ke Logcat untuk debugging
            null // Kembalikan null jika gagal
        }
    }

    /**
     * Menyimpan (menimpa) seluruh data menu ke file 'menu_items.json' di penyimpanan internal.
     * Fungsi ini dipanggil setelah Admin menambah, mengedit, atau menghapus produk.
     *
     * @param menuData Objek MenuData yang berisi daftar produk terbaru.
     */
    fun saveMenuData(menuData: MenuData) {
        try {
            val file = File(context.filesDir, MENU_FILE_NAME)
            file.writer().use { writer ->
                gson.toJson(menuData, writer)
            }
        } catch (e: IOException) {
            e.printStackTrace() // Cetak error ke Logcat untuk debugging
        }
    }

    // --- FUNGSI UNTUK ORDERS & VOUCHERS (BISA DITAMBAHKAN NANTI) ---

    /*
    // Contoh kerangka fungsi untuk Orders

    fun getOrders(): OrdersData? {
        // Logika mirip seperti getMenuData(), tapi untuk file ORDERS_FILE_NAME
        // ...
        return null
    }

    fun saveOrders(ordersData: OrdersData) {
        // Logika mirip seperti saveMenuData(), tapi untuk file ORDERS_FILE_NAME
        // ...
    }
    */

}