package com.example.map_umkm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FormAlamatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_alamat)

        if (savedInstanceState == null) {
            val fragment = AddEditAddressFragment()

            val addressId = intent.getStringExtra("addressId")

            if (addressId != null) {
                val bundle = Bundle()
                bundle.putString("addressId", addressId)
                fragment.arguments = bundle
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.container_form, fragment)
                .commit()
        }
    }
}