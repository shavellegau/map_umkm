package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- 1) cek session dulu sebelum setContentView
        val prefs = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val savedEmail = prefs.getString("email", null)
        val savedRole = prefs.getString("role", null)
        if (savedEmail != null && savedRole != null) {
            // sudah login sebelumnya -> langsung masuk sesuai role
            val target = if (savedRole == "admin") AdminActivity::class.java else MainActivity::class.java
            val i = Intent(this, target)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val role = dbHelper.checkUser(email, password)
            if (role != null) {
                // --- simpan session
                val editor = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE).edit()
                editor.putString("email", email)
                editor.putString("role", role)
                editor.apply()

                // --- buka activity sesuai role dan clear backstack
                val target = if (role == "admin") AdminActivity::class.java else MainActivity::class.java
                val i = Intent(this, target)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                finish()
            } else {
                Toast.makeText(this, "Login gagal, periksa email/password", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
