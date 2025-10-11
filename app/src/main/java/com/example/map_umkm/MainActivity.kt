package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.map_umkm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. LOGIC OTENTIKASI (PUNYA ANDA)
        val prefs = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val email = prefs.getString("userEmail", null)
        val role = prefs.getString("userRole", null)

        // Redirect ke Login jika belum login
        if (email == null) {
            val i = Intent(this, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            finish()
            return
        }

        // Redirect ke AdminActivity jika role adalah admin
        if (role == "admin") {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
            return
        }

        // 2. SETUP VIEW BINDING & TAMPILAN UTAMA (HANYA JIKA SUDAH LOGIN & BUKAN ADMIN)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. SETUP NAVIGATION COMPONENT

        // Dapatkan NavHostFragment dari layout (ID asumsikan R.id.nav_host_fragment)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Tautkan BottomNavigationView ke NavController menggunakan View Binding
        // Asumsi ID di activity_main.xml adalah @id/bottom_nav (yang diakses via binding.bottomNav)
        binding.bottomNav.setupWithNavController(navController)
    }
}
