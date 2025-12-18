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
        supportActionBar?.hide()
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPref = getSharedPreferences("AppPref", Context.MODE_PRIVATE)
            val isOnboardingFinished = sharedPref.getBoolean("Finished", false)
            if (isOnboardingFinished) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, OnboardingActivity::class.java)
                startActivity(intent)
            }
            finish()

        }, 3000) 
    }
}