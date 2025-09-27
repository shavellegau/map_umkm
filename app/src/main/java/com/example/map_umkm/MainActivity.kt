package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.Button
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Cek apakah user sudah login (dari SharedPreferences)
        val prefs = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val email = prefs.getString("userEmail", null)
        val role = prefs.getString("userRole", null)

        if (email == null) {
            val i = Intent(this, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            finish()
            return
        }

// kalau mau arahkan admin ke AdminActivity
        if (role == "admin") {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
            return
        }


        // 🔹 Kalau sudah login, baru load tampilan utama
        setContentView(R.layout.activity_main)

        // Ambil NavHostFragment dari layout
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Hubungkan BottomNavigationView dengan NavController
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavView.setupWithNavController(navController)


    }
}
