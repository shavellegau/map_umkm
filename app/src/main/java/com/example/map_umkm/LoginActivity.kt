package com.example.map_umkm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.map_umkm.model.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.map_umkm.model.ApiClient



class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button  // tombol register

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister) // inisialisasi tombol register

        // Login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ApiClient.instance.login(email, pass).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val user = response.body()?.user

                        // simpan session pakai SharedPreferences
                        val prefs = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                        prefs.edit()
                            .putInt("userId", user?.id ?: -1)
                            .putString("userEmail", user?.email)
                            .putString("userRole", user?.role)
                            .apply()

                        // arahkan ke activity sesuai role
                        if (user?.role == "admin") {
                            startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
                        } else {
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        }
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            response.body()?.message ?: "Login gagal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Pindah ke RegisterActivity
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}

