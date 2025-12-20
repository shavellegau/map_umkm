package com.example.map_umkm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FormAlamatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_alamat) // Pastikan layout XML ini ada

        if (savedInstanceState == null) {
            // 1. Siapkan Fragment Form
            val fragment = AddEditAddressFragment()

            // 2. Cek apakah ada ID alamat (Mode Edit) dari Intent
            val addressId = intent.getStringExtra("addressId")

            // 3. Jika ada, oper ke Fragment via Arguments
            if (addressId != null) {
                val bundle = Bundle()
                bundle.putString("addressId", addressId)
                fragment.arguments = bundle
            }

            // 4. Tampilkan Fragment di dalam container
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_form, fragment)
                .commit()
        }
    }
}