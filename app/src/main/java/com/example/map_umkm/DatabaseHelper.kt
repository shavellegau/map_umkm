package com.example.map_umkm

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDB"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USERS = "users"
        private const val COL_ID = "id"
        private const val COL_EMAIL = "email"
        private const val COL_PASSWORD = "password"
        private const val COL_ROLE = "role"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_USERS (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_EMAIL TEXT UNIQUE, " +
                "$COL_PASSWORD TEXT, " +
                "$COL_ROLE TEXT)"
        db?.execSQL(createTable)

        // 🔹 Insert default admin
        val values = ContentValues().apply {
            put(COL_EMAIL, "admin@gmail.com")
            put(COL_PASSWORD, "admin123")
            put(COL_ROLE, "admin")
        }
        db?.insert(TABLE_USERS, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // 🔹 Register User Baru
    fun registerUser(email: String, password: String, role: String = "user"): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_EMAIL, email)
            put(COL_PASSWORD, password)
            put(COL_ROLE, role)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    // 🔹 Cek Login
    fun checkUser(email: String, password: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ROLE FROM $TABLE_USERS WHERE $COL_EMAIL=? AND $COL_PASSWORD=?",
            arrayOf(email, password)
        )
        var role: String? = null
        if (cursor.moveToFirst()) {
            role = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return role
    }
}
