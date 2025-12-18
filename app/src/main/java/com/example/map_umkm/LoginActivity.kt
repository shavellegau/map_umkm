package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView 
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText 
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText

    private lateinit var btnLogin: Button
    private lateinit var btnRegister: TextView

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
            handleFirebaseLogin()
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

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
                    
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val role = documentSnapshot.getString("role") ?: "user"
                                val name = documentSnapshot.getString("name") ?: email

                                onLoginSuccess(name, email, role, uid)
                                updateFCMToken(uid)
                            } else {
                                Toast.makeText(this, "Data pengguna tidak ditemukan di database.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("LoginActivity", "Gagal mengambil data user: ", e)
                            Toast.makeText(this, "Gagal mengambil data pengguna.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Terjadi kesalahan saat otentikasi.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Login gagal: ", e)
                Toast.makeText(this, "Login gagal: Email atau Password salah.", Toast.LENGTH_LONG).show()
            }
    }

    private fun onLoginSuccess(name: String, email: String, role: String, uid: String) {
        val prefs = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("userName", name)
            .putString("userEmail", email)
            .putString("userRole", role)
            .putString("userUid", uid)
            .apply()

        val intent = Intent(this, MainActivity::class.java)
        if (role == "admin") {
            Toast.makeText(this, "Selamat datang, Admin!", Toast.LENGTH_SHORT).show()
            intent.putExtra("openAdmin", true)
        } else {
            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
        }
        startActivity(intent)
        finishAffinity()
    }

    private fun updateFCMToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (token != null) {
                    getSharedPreferences("USER_PREFS", MODE_PRIVATE).edit()
                        .putString("fcm_token", token)
                        .apply()

                    val tokenData = mapOf("fcmToken" to token)
                    db.collection("users").document(userId)
                        .set(tokenData, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener { Log.d("LoginActivity", "FCM Token berhasil diperbarui.") }
                        .addOnFailureListener { e -> Log.e("LoginActivity", "Gagal menyimpan FCM token: ${e.message}") }
                }
            }
        }
    }
}