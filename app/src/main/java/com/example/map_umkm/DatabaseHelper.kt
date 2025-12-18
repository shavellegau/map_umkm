package com.example.map_umkm

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "TukuUserDB.db" 
        private const val DATABASE_VERSION = 1
        private const val TABLE_USERS = "users"
        private const val COL_ID = "id"
        private const val COL_NAME = "name" 
        private const val COL_EMAIL = "email"
        private const val COL_PASSWORD = "password"
        private const val COL_ROLE = "role"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_USERS (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_NAME TEXT, " + 
                "$COL_EMAIL TEXT UNIQUE, " +
                "$COL_PASSWORD TEXT, " +
                "$COL_ROLE TEXT)"
        db?.execSQL(createTable)

        
        addDefaultUsers(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    
    private fun addDefaultUsers(db: SQLiteDatabase?) {
        
        val adminValues = ContentValues().apply {
            put(COL_NAME, "Admin Tuku")
            put(COL_EMAIL, "admin@gmail.com")
            put(COL_PASSWORD, "admin123")
            put(COL_ROLE, "admin")
        }
        db?.insert(TABLE_USERS, null, adminValues)

        
        val userValues = ContentValues().apply {
            put(COL_NAME, "Abdul")
            put(COL_EMAIL, "abdul@gmail.com")
            put(COL_PASSWORD, "abdul123")
            put(COL_ROLE, "user")
        }
        db?.insert(TABLE_USERS, null, userValues)
    }

    
    fun registerUser(name: String, email: String, password: String): Boolean {
        val db = this.writableDatabase
        
        val cursor = db.rawQuery("SELECT $COL_ID FROM $TABLE_USERS WHERE $COL_EMAIL=?", arrayOf(email))
        if (cursor.count > 0) {
            cursor.close()
            return false 
        }
        cursor.close()

        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_EMAIL, email)
            put(COL_PASSWORD, password)
            put(COL_ROLE, "user") 
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    
    fun checkUser(email: String, password: String): Map<String, String>? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_NAME, $COL_ROLE FROM $TABLE_USERS WHERE $COL_EMAIL=? AND $COL_PASSWORD=?",
            arrayOf(email, password)
        )
        var userData: Map<String, String>? = null
        if (cursor.moveToFirst()) {
            
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
            val role = cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE))
            userData = mapOf("name" to name, "role" to role)
        }
        cursor.close()
        db.close()
        return userData
    }
}
