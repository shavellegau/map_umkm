package com.example.map_umkm

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// HAPUS IMPORT FIREBASE
// import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    // [DIUBAH] Gunakan DatabaseHelper, bukan Firebase
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inisialisasi DatabaseHelper
        dbHelper = DatabaseHelper(this)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)

        tvLogin.setOnClickListener {
            // Kembali ke halaman login
            finish()
        }

        btnRegister.setOnClickListener {
            handleRegister()
        }
    }

    // [DIUBAH] Fungsi untuk mendaftar ke database lokal
    private fun handleRegister() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format email tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show()
            return
        }

        // Panggil fungsi register dari DatabaseHelper
        val isSuccess = dbHelper.registerUser(name, email, password)

        if (isSuccess) {
            Toast.makeText(this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show()
            finish() // Tutup halaman register dan kembali ke login
        } else {
            Toast.makeText(this, "Email sudah terdaftar secara lokal.", Toast.LENGTH_SHORT).show()
        }
    }
}
