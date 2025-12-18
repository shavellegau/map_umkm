package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton


import com.example.map_umkm.adapter.OnboardingAdapter
import com.example.map_umkm.model.OnboardingItem


class OnboardingActivity : AppCompatActivity() {

    
    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var btnNext: FloatingActionButton
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        
        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        val tvSkip = findViewById<TextView>(R.id.tvSkip)

        
        val items = listOf(
            
            OnboardingItem(
                
                R.drawable.gambar_onboarding1,
                "Halo, Tetangga!",
                "Selamat datang di rumah digital Tuku. Jajan kopi dan camilan favorit kini makin dekat di genggaman."
            ),

            
            OnboardingItem(
                
                R.drawable.gambar_onboarding2,
                "Tuku di Saku!",
                "Dari Tetangga, untuk Tetangga. Nikmati kemudahan memesan Kopi Tuku kapan pun kamu mau."
            )
        )

        
        onboardingAdapter = OnboardingAdapter(items)
        viewPager.adapter = onboardingAdapter

        
        btnNext.setOnClickListener {
            
            if (viewPager.currentItem + 1 < onboardingAdapter.itemCount) {
                
                viewPager.currentItem += 1
            } else {
                
                finishOnboarding()
            }
        }
        tvSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        
        val sharedPref = getSharedPreferences("AppPref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}