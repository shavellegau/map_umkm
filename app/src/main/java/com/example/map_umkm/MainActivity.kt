package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.map_umkm.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.example.map_umkm.admin.AdminNotificationFragment
import com.example.map_umkm.AdminOrdersFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ======================================================
        // 1. Cek sesi login
        // ======================================================
        val prefs = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val email = prefs.getString("userEmail", null)

        if (email == null) {
            val i = Intent(this, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            finish()
            return
        }

        // ======================================================
        // 2. Setup UI
        // ======================================================
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = binding.bottomNav

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        bottomNavigationView.setupWithNavController(navController)

        // ======================================================
        // 🔥 PERBAIKAN: LOGIKA MENYEMBUNYIKAN NAVBAR 🔥
        // ======================================================
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // Daftar Halaman yang TIDAK BOLEH ada Navbar
                R.id.paymentFragment,
                R.id.paymentSuccessFragment,
                R.id.qrisFragment,
                R.id.productDetailFragment,
                R.id.adminDashboardFragment,
                R.id.adminOrdersFragment,      // Tambahkan fragment admin lain jika ada
                R.id.adminNotificationFragment,
                R.id.adminMenuFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    // Halaman Utama (Home, Cart, Profile) -> Munculkan Navbar
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }

        // ======================================================
        // 3. Admin Mode → buka fragment admin dashboard
        // ======================================================
        val openAdmin = intent.getBooleanExtra("openAdmin", false)
        if (openAdmin) {
            navController.navigate(R.id.adminDashboardFragment)
            // Admin tidak butuh bottom nav (sudah dihandle listener di atas, tapi ini untuk safety)
            bottomNavigationView.visibility = View.GONE
        }

        // ======================================================
        // 4. Save Token FCM
        // ======================================================
        saveUserFCMToken()

        // ======================================================
        // 5. Subscribe ke topik promo
        // ======================================================
        FirebaseMessaging.getInstance().subscribeToTopic("promo")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM_SUB", "Berhasil subscribe ke promo")
                } else {
                    Log.e("FCM_SUB", "Gagal subscribe", task.exception)
                }
            }
    }

    // ======================================================
    // 6. Simpan token FCM ke SharedPrefs
    // ======================================================
    private fun saveUserFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM_TOKEN", "Gagal ambil token", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM_TOKEN", "Token user: $token")

            getSharedPreferences("USER_PREFS", MODE_PRIVATE)
                .edit()
                .putString("fcm_token", token)
                .apply()
        }
    }
}