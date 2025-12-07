package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    // [DIHAPUS] DatabaseHelper tidak lagi digunakan untuk proses login.
    // private lateinit var dbHelper: DatabaseHelper

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
            handleFirebaseLogin() // [FIXED] Selalu panggil fungsi login Firebase.
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * [FIXED TOTAL] Logika login sekarang HANYA dan SELALU ke Firebase.
     * Tidak ada lagi pengecekan ke database lokal.
     */
    private fun handleFirebaseLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    // Setelah login Firebase berhasil, ambil data user dari Firestore.
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val role = documentSnapshot.getString("role") ?: "user"
                                val name = documentSnapshot.getString("name") ?: email // Fallback ke email jika nama null

                                // [FIXED] Panggil onLoginSuccess dengan UID yang VALID dari Firebase.
                                onLoginSuccess(name, email, role, uid)
                                // Perbarui juga token FCM setelah semua data siap.
                                updateFCMToken(uid)

                            } else {
                                Toast.makeText(this, "Data pengguna tidak ditemukan di database.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("LoginActivity", "Gagal mengambil data user dari Firestore: ", e)
                            Toast.makeText(this, "Gagal mengambil data pengguna.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Terjadi kesalahan saat otentikasi.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Jika login Firebase gagal (email/password salah, user tidak ada).
                Log.e("LoginActivity", "Firebase signInWithEmailAndPassword gagal: ", e)
                Toast.makeText(this, "Login gagal: Email atau Password salah.", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * [FIXED] Fungsi ini sekarang PASTI menerima UID yang valid dan tidak null.
     */
    private fun onLoginSuccess(name: String, email: String, role: String, uid: String) {
        // Simpan sesi login ke SharedPreferences dengan UID yang valid.
        val prefs = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("userName", name)
            .putString("userEmail", email)
            .putString("userRole", role)
            .putString("userUid", uid) // UID PASTI ADA, tidak akan null lagi.
            .apply()

        // Arahkan ke activity utama.
        val intent = Intent(this, MainActivity::class.java)
        if (role == "admin") {
            Toast.makeText(this, "Selamat datang, Admin!", Toast.LENGTH_SHORT).show()
            intent.putExtra("openAdmin", true)
        } else {
            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
        }
        startActivity(intent)
        finishAffinity() // Tutup semua activity sebelumnya agar tidak bisa kembali ke halaman login.
    }

    /**
     * Memperbarui token FCM di Firestore setelah pengguna berhasil login.
     * @param userId UID pengguna yang sedang login.
     */
    private fun updateFCMToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (token != null) {
                    // Simpan token ke SharedPreferences juga
                    getSharedPreferences("USER_PREFS", MODE_PRIVATE).edit()
                        .putString("fcm_token", token)
                        .apply()

                    // Update token di Firestore
                    val tokenData = mapOf("fcmToken" to token)
                    db.collection("users").document(userId)
                        .set(tokenData, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener { Log.d("LoginActivity", "FCM Token berhasil diperbarui.") }
                        .addOnFailureListener { e -> Log.e("LoginActivity", "Gagal menyimpan FCM token: ${e.message}") }
                }
            } else {
                Log.w("LoginActivity", "Gagal mendapatkan FCM token", task.exception)
            }
        }
    }
}
