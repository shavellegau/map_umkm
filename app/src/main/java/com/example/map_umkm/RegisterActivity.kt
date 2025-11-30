package com.example.map_umkm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot // Import untuk hasil query

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etReferralCode: EditText // <-- DITAMBAH
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Menyimpan kode referral yang valid dari pengguna lama
    private var validReferralCode: String? = null
    // Menyimpan ID dokumen pengguna lama (referrer)
    private var referrerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etReferralCode = findViewById(R.id.etReferralCode) // <-- INISIALISASI
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

    private fun handleRegister() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val referralCode = etReferralCode.text.toString().trim() // <-- AMBIL INPUT KODE

        // --- Validasi Input Dasar ---
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

        // --- 1. Validasi Kode Referral (Jika Diisi) ---
        if (referralCode.isNotEmpty()) {
            // Cek ke Firestore apakah kode ini valid
            checkReferralCodeValidity(name, email, password, referralCode)
        } else {
            // Lanjut registrasi tanpa kode referral
            processFirebaseRegistration(name, email, password, null)
        }
    }

    /**
     * Memeriksa apakah kode referral yang dimasukkan pengguna (referralCode) ada di Firestore.
     */
    private fun checkReferralCodeValidity(name: String, email: String, password: String, referralCode: String) {
        // Cari dokumen user yang memiliki 'ownReferralCode' sama dengan input
        db.collection("users")
            .whereEqualTo("ownReferralCode", referralCode)
            .limit(1) // Hanya butuh satu hasil
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Kode valid! Simpan kodenya dan ID referrer
                    val document = querySnapshot.documents.first()
                    validReferralCode = referralCode
                    referrerId = document.id

                    // Lanjut ke langkah registrasi Auth
                    processFirebaseRegistration(name, email, password, validReferralCode)

                } else {
                    // Kode tidak ditemukan
                    Toast.makeText(this, "Kode Referral tidak ditemukan atau tidak valid!", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("RegisterActivity", "Gagal cek kode referral: ${e.message}", e)
                Toast.makeText(this, "Kesalahan koneksi saat validasi referral.", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Melakukan registrasi di Firebase Auth dan menyimpan data ke Firestore.
     */
    private fun processFirebaseRegistration(name: String, email: String, password: String, usedCode: String?) {

        // --- Proses Registrasi Firebase Auth ---
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 1. Registrasi Auth Berhasil
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {
                        // 2. Simpan Data Tambahan ke Firestore

                        // [REFERRAL]: Buat kode unik untuk pengguna baru (6 karakter pertama UID)
                        val ownCode = uid.substring(0, 6).uppercase()

                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "role" to "user", // Default role
                            "ownReferralCode" to ownCode, // Kode referral unik milik pengguna ini
                            "usedReferralCode" to (usedCode ?: ""), // Kode referral yang digunakannya (jika ada)

                            // Status awal untuk Cloud Function
                            "referralRewardStatus" to if (usedCode != null) "pending" else "none",

                            // Saldo Reward Awal
                            "rewardBalance" to 0
                        ) as Map<String, Any>

                        db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                // 3. Pendaftaran Selesai
                                Toast.makeText(this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show()
                                finish() // Kembali ke halaman login
                            }
                            .addOnFailureListener { e ->
                                // Gagal menyimpan data ke Firestore. Hapus akun Auth yang sudah dibuat (Rollback).
                                user.delete()
                                Toast.makeText(this, "Gagal menyimpan data user: ${e.message}", Toast.LENGTH_LONG).show()
                                Log.e("RegisterActivity", "Rollback: Akun Auth dihapus karena Firestore gagal.", e)
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