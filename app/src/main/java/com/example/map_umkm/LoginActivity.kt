package com.example.map_umkm
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

        // 🔹 Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 🔹 Tombol Login ditekan
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔹 Login ke Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid

                    if (uid != null) {
                        // 🔹 Jika admin login dan belum ada di Firestore → otomatis tambahkan
                        if (email == "admin@gmail.com") {
                            val adminData = hashMapOf(
                                "name" to "Admin",
                                "email" to email,
                                "role" to "admin"
                            )

                            db.collection("users").document(uid).set(adminData)
                                .addOnSuccessListener {
                                    goToDashboard(uid, email)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Gagal buat data admin: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // 🔹 Ambil data user dari Firestore
                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        goToDashboard(uid, email)
                                    } else {
                                        Toast.makeText(this, "Data user tidak ditemukan.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Gagal ambil data: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "UID user tidak valid.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Login gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // 🔹 Pindah ke halaman register
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // 🔹 Fungsi untuk arahkan ke dashboard sesuai role
    private fun goToDashboard(uid: String, email: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "user"
                val name = document.getString("name") ?: ""

                // 🔹 Simpan sesi user
                val prefs = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                prefs.edit()
                    .putString("userUid", uid)
                    .putString("userName", name)
                    .putString("userEmail", email)
                    .putString("userRole", role)
                    .apply()

                // 🔹 Arahkan sesuai role
                if (role == "admin") {
                    startActivity(Intent(this, AdminActivity::class.java))
                    Toast.makeText(this, "Selamat datang Admin!", Toast.LENGTH_SHORT).show()
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
                }

                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal baca data user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
