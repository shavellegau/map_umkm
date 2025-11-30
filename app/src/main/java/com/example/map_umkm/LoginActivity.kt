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

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnLogin.setOnClickListener {
            handleFirebaseLogin() // [FIXED] Panggil fungsi login Firebase
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // [FIXED TOTAL] Logika login sekarang hanya ke Firebase
    private fun handleFirebaseLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    // Ambil data user dari Firestore untuk mendapatkan role & nama
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val role = document.getString("role") ?: "user"
                                val name = document.getString("name") ?: email
                                // [FIXED] Panggil onLoginSuccess dengan UID yang valid
                                onLoginSuccess(name, email, role, uid)
                            } else {
                                Toast.makeText(this, "Data user tidak ditemukan di database.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal mengambil data user.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Terjadi kesalahan saat otentikasi.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                // Jika login Firebase gagal
                Toast.makeText(this, "Login gagal: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    // [FIXED] Fungsi ini sekarang PASTI menerima UID
    private fun onLoginSuccess(name: String, email: String, role: String, uid: String) {
        val prefs = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("userName", name)
            .putString("userEmail", email)
            .putString("userRole", role)
            .putString("userUid", uid) // UID PASTI ADA
            .apply()

        val intent = Intent(this, MainActivity::class.java)
        if (role == "admin") {
            Toast.makeText(this, "Selamat datang, Admin!", Toast.LENGTH_SHORT).show()
            intent.putExtra("openAdmin", true)
        } else {
            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
        }
        startActivity(intent)
        finish()
    }
}
