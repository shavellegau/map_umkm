package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Cek session
        val prefs = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val role = prefs.getString("userRole", null)

        // 🔹 Kalau belum login / bukan admin
        if (role != "admin") {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 🔹 Kalau admin -> tampilkan layout admin
        setContentView(R.layout.activity_admin)

        // 🔹 Tombol logout (pastikan ada di activity_admin.xml)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            prefs.edit().clear().apply() // hapus session

            val i = Intent(this, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            finish()
        }
    }
}
