package com.example.map_umkm

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    // [DITAMBAH] Inisialisasi Firebase Authentication dan Firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // [DITAMBAH] Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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

    // [DIUBAH TOTAL] Fungsi untuk mendaftar menggunakan Firebase
    private fun handleRegister() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // --- Validasi Input ---
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

        // --- Proses Registrasi Firebase ---
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 1. Registrasi Auth Berhasil
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {
                        // 2. Simpan Data Tambahan (Nama & Role) ke Firestore
                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "role" to "user" // Default role untuk registrasi baru
                        )

                        db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                // 3. Pendaftaran Selesai
                                Toast.makeText(this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show()
                                finish() // Kembali ke halaman login
                            }
                            .addOnFailureListener { e ->
                                // Gagal menyimpan data ke Firestore. Hapus akun Auth yang sudah dibuat agar tidak jadi akun "hantu".
                                user.delete()
                                Toast.makeText(this, "Gagal menyimpan data user: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }

                } else {
                    // Registrasi Auth Gagal (Email sudah digunakan, dll.)
                    val errorMessage = task.exception?.message ?: "Registrasi gagal, coba lagi."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}
