// [FIXED] Kesalahan ketik pada package dan import sudah diperbaiki
package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    // Tambahkan DatabaseHelper
    private lateinit var dbHelper: DatabaseHelper

    // Firebase tetap ada
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        // Inisialisasi DatabaseHelper dan Firebase
        dbHelper = DatabaseHelper(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnLogin.setOnClickListener {
            handleLogin()
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Logika login digabung
    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Coba login ke database LOKAL terlebih dahulu
        val localUserData = dbHelper.checkUser(email, password)
        if (localUserData != null) {
            val name = localUserData["name"] ?: ""
            val role = localUserData["role"] ?: "user"
            // Jika berhasil, langsung masuk tanpa perlu Firebase
            onLoginSuccess(name, email, role, null) // uid null karena dari lokal
            return
        }

        // 2. Jika di lokal tidak ada, baru coba login ke FIREBASE
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { document ->
                            val role = document.getString("role") ?: "user"
                            val name = document.getString("name") ?: ""
                            // Login via Firebase berhasil
                            onLoginSuccess(name, email, role, uid)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal mengambil data user dari Firestore.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                // Jika di lokal dan Firebase gagal, tampilkan error
                Toast.makeText(this, "Email atau Password salah.", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi ini sekarang menerima uid yang bisa null
    private fun onLoginSuccess(name: String, email: String, role: String, uid: String?) {
        // Simpan sesi login ke SharedPreferences
        val prefs = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("userName", name)
            .putString("userEmail", email)
            .putString("userRole", role)
            .putString("userUid", uid) // Simpan UID jika ada (dari Firebase)
            .apply()

        // Arahkan ke activity yang sesuai berdasarkan role
        if (role == "admin") {
            Toast.makeText(this, "Selamat datang, Admin!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AdminActivity::class.java))
        } else {
            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish() // Tutup LoginActivity agar tidak bisa kembali
    }
}
