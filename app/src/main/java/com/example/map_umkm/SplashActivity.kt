package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hilangkan Action Bar agar full screen (Opsional)
        supportActionBar?.hide()

        // Handler untuk delay selama 3 detik
        Handler(Looper.getMainLooper()).postDelayed({

            // 1. Cek Shared Preferences (Database kecil di HP)
            val sharedPref = getSharedPreferences("AppPref", Context.MODE_PRIVATE)

            // Ambil data boolean, default-nya false (belum pernah buka)
            val isOnboardingFinished = sharedPref.getBoolean("Finished", false)

            if (isOnboardingFinished) {
                // 2a. Jika user sudah pernah onboarding, langsung ke LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // 2b. Jika user belum pernah (baru install), ke OnboardingActivity
                val intent = Intent(this, OnboardingActivity::class.java)
                startActivity(intent)
            }

            // Tutup SplashActivity agar user tidak bisa kembali ke sini saat tekan tombol Back
            finish()

        }, 3000) // 3000 ms = 3 detik
    }
}