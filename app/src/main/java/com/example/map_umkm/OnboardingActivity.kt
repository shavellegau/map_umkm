package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton

// Pastikan import ini sesuai dengan package tempat kamu menyimpan file Adapter & Model
import com.example.map_umkm.adapter.OnboardingAdapter
import com.example.map_umkm.model.OnboardingItem
// Jika OnboardingItem ada di folder adapter, ubah jadi: .adapter.OnboardingItem

class OnboardingActivity : AppCompatActivity() {

    // Deklarasi variabel UI
    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var btnNext: FloatingActionButton
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // 1. Inisialisasi View
        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        val tvSkip = findViewById<TextView>(R.id.tvSkip)

        // 2. Siapkan Data Slide (Sesuai Desain TUKU)
        val items = listOf(
            // --- Slide 1: Halo, Tetangga! ---
            OnboardingItem(
                // Pastikan nama file gambar ini ada di res/drawable
                R.drawable.gambar_onboarding1,
                "Halo, Tetangga!",
                "Selamat datang di rumah digital Tuku. Jajan kopi dan camilan favorit kini makin dekat di genggaman."
            ),

            // --- Slide 2: Tuku di Saku! (Data Baru) ---
            OnboardingItem(
                // Pastikan kamu sudah memasukkan gambar slide kedua ke res/drawable
                R.drawable.gambar_onboarding2,
                "Tuku di Saku!",
                "Dari Tetangga, untuk Tetangga. Nikmati kemudahan memesan Kopi Tuku kapan pun kamu mau."
            )
        )

        // 3. Pasang Adapter ke ViewPager
        onboardingAdapter = OnboardingAdapter(items)
        viewPager.adapter = onboardingAdapter

        // 4. Logic Tombol Next (Panah)
        btnNext.setOnClickListener {
            // Cek apakah masih ada halaman selanjutnya?
            if (viewPager.currentItem + 1 < onboardingAdapter.itemCount) {
                // Jika ada, geser ke halaman berikutnya
                viewPager.currentItem += 1
            } else {
                // Jika sudah di halaman terakhir, selesai & masuk Login
                finishOnboarding()
            }
        }

        // 5. Logic Tombol Skip (Lewati)
        tvSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    // Fungsi untuk menyelesaikan onboarding dan menyimpan statusnya
    private fun finishOnboarding() {
        // Simpan data bahwa user sudah pernah baca onboarding
        val sharedPref = getSharedPreferences("AppPref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("Finished", true)
        editor.apply()

        // Pindah ke halaman Login
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

        // Tutup activity ini agar tidak bisa kembali saat tekan tombol Back HP
        finish()
    }
}