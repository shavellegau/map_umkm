package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.map_umkm.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging // <-- Import Wajib

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- 1. LOGIC OTENTIKASI USER ---
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

        if (role == "admin") {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
            return
        }

        // ==================================================================
        // 2. SIMPAN TOKEN FCM USER
        // ==================================================================
        saveUserFCMToken()

        // ==================================================================
        // 3. SUBSCRIBE KE TOPIK "promo"
        // ==================================================================
        FirebaseMessaging.getInstance().subscribeToTopic("promo")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM_SUBSCRIBE", "Berhasil subscribe ke Promo")
                } else {
                    Log.e("FCM_SUBSCRIBE", "Gagal subscribe ke promo", task.exception)
                }
            }

        // ==================================================================
        // 4. SETUP UI
        // ==================================================================
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = binding.bottomNav

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        bottomNavigationView.setupWithNavController(navController)
    }

    // ==================================================================
    // FUNGSI SIMPAN TOKEN FCM KE SHAREDPREFS
    // ==================================================================
    private fun saveUserFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_TOKEN", "Gagal mengambil token registrasi FCM", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM_TOKEN", "Token User saat ini: $token")

            getSharedPreferences("USER_PREFS", MODE_PRIVATE)
                .edit()
                .putString("fcm_token", token)
                .apply()
        }
    }
}
